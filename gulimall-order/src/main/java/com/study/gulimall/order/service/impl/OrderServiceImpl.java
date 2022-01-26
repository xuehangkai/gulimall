package com.study.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.study.common.exception.NoStockException;
import com.study.common.to.SkuHasStockVo;
import com.study.common.to.mq.SeckillOrderTo;
import com.study.common.utils.R;
import com.study.common.vo.MemberEntityVo;
import com.study.common.vo.OrderVo;
import com.study.gulimall.order.dao.OrderItemDao;
import com.study.gulimall.order.entity.OrderItemEntity;
import com.study.gulimall.order.entity.PaymentInfoEntity;
import com.study.gulimall.order.enume.OrderStatusEnum;
import com.study.gulimall.order.feign.CartFeign;
import com.study.gulimall.order.feign.MemberFeign;
import com.study.gulimall.order.feign.ProductFeignService;
import com.study.gulimall.order.feign.WmsFeignService;
import com.study.gulimall.order.interceptor.LoginUserInterceptor;
import com.study.gulimall.order.service.OrderItemService;
import com.study.gulimall.order.service.PaymentInfoService;
import com.study.gulimall.order.to.OrderCreatTo;
import com.study.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.order.dao.OrderDao;
import com.study.gulimall.order.entity.OrderEntity;
import com.study.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import static com.study.gulimall.order.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal=new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeign memberFeign;
    @Autowired
    CartFeign cartFeign;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        MemberEntityVo memberEntityVo = LoginUserInterceptor.toThreadLocal.get();


        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();


        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberReceiveAddressVo> address = memberFeign.getAddress(memberEntityVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cart = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeign.getCurrentUserCartItems();
            confirmVo.setItems(currentUserCartItems);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> {
                return item.getSkuId();
            }).collect(Collectors.toList());
            R r = wmsFeignService.getSkusHasStock(collect);
            List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            if(data!=null){
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStocks(collect1);
            }
        }, executor);

        Integer integration = memberEntityVo.getIntegration();
        confirmVo.setIntegration(integration);

        CompletableFuture.allOf(getAddress,cart).get();

        if(confirmVo.getItems()!=null){
            BigDecimal sum = new BigDecimal("0");
            for (OrderItemVo item : confirmVo.getItems()) {
                sum=sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
            confirmVo.setTotal(sum);
        }
        if(confirmVo.getItems()!=null){
            BigDecimal sum = new BigDecimal("0");
            for (OrderItemVo item : confirmVo.getItems()) {
                sum=sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
            confirmVo.setPayPrice(sum);
        }

        String token = UUID.randomUUID().toString().replace("_", "");
        confirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(USER_ORDER_TOKEN_PREFIX +memberEntityVo.getId(),token,30, TimeUnit.MINUTES);

        return confirmVo;
    }

    @Override
    @Transactional
    //@GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo=new SubmitOrderResponseVo();
        orderSubmitVoThreadLocal.set(vo);

        MemberEntityVo memberEntityVo = LoginUserInterceptor.toThreadLocal.get();
        responseVo.setCode(0);
        String orderToken = vo.getOrderToken();

        String script="if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

        String redisToken = redisTemplate.opsForValue().get(USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId());
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId()), orderToken);

        if(result==0L){
            responseVo.setCode(1);
            return responseVo;
        }else {
            OrderCreatTo order = creatOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01){
                saveOrder(order);

                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode()==0){
                    responseVo.setOrder(order.getOrder());
                    //int i=10/0;

                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

                    return responseVo;
                }else {
                    String msg= (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            }else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

        return order_sn;
    }

    @Override
    @Transactional
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity order = this.getById(orderEntity);
        if(order.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity update = new OrderEntity();
            update.setId(order.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderVo orderVo = new OrderVo();
            BeanUtils.copyProperties(order,orderVo);
            try {
                rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderVo);
            }catch (Exception e){

            }


        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo=new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        payVo.setTotal_amount(order.getPayAmount().setScale(2,BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(order.getOrderSn());
        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        OrderItemEntity itemEntity = order_sn.get(0);
        payVo.setSubject(itemEntity.getSkuName());
        payVo.setBody(itemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberEntityVo memberEntityVo = LoginUserInterceptor.toThreadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberEntityVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> order_sn = page.getRecords().stream().map(orderEntity -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderEntity.getOrderSn()));
            orderEntity.setItemEntityList(itemEntities);
            return orderEntity;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);

        return new PageUtils(page);

    }

    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());

        boolean save = paymentInfoService.save(paymentInfoEntity);

        if(payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")){
            String out_trade_no = payAsyncVo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(out_trade_no,OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void creatSeckillOrder(SeckillOrderTo seckillOrderTo) {

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setPayAmount(seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum())));
        boolean save = this.save(orderEntity);
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setSkuId(seckillOrderTo.getSkuId());
        itemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        itemEntity.setRealAmount(seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum())));
        itemEntity.setSkuQuantity(seckillOrderTo.getNum());
        boolean save1 = orderItemService.save(itemEntity);

    }


    private void saveOrder(OrderCreatTo order) {

        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);


    }

    private OrderCreatTo creatOrder(){

        OrderCreatTo orderCreatTo = new OrderCreatTo();
        String orderSn = IdWorker.getTimeId();

        OrderEntity orderEntity = buildOrder(orderSn);


        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);


        computPrice(orderEntity,itemEntities);
        orderCreatTo.setOrder(orderEntity);
        orderCreatTo.setOrderItems(itemEntities);

        return  orderCreatTo;
    }

    private void computPrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");

        BigDecimal gift = new BigDecimal("0");
        BigDecimal growth = new BigDecimal("0");
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            BigDecimal couponAmount = itemEntity.getCouponAmount();
            BigDecimal integrationAmount = itemEntity.getIntegrationAmount();
            BigDecimal promotionAmount = itemEntity.getPromotionAmount();

            gift =gift.add(new BigDecimal(itemEntity.getGiftIntegration()));
            growth =growth.add(new BigDecimal(itemEntity.getGiftGrowth()));

            total =total.add(realAmount);
            coupon =coupon.add(couponAmount);
            integration =integration.add(integrationAmount);
            promotion = promotion.add(promotionAmount);

        }
        orderEntity.setTotalAmount(total);

        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        MemberEntityVo memberEntityVo = LoginUserInterceptor.toThreadLocal.get();

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberEntityVo.getId());

        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {});
        orderEntity.setFreightAmount(data.getFare());
        orderEntity.setReceiverCity(data.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        orderEntity.setReceiverName(data.getAddress().getName());
        orderEntity.setReceiverPhone(data.getAddress().getPhone());
        orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
        orderEntity.setReceiverProvince(data.getAddress().getProvince());
        orderEntity.setReceiverRegion(data.getAddress().getRegion());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);


        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeign.getCurrentUserCartItems();
        if(currentUserCartItems!=null && currentUserCartItems.size()>0){
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity=new OrderItemEntity();

        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());


        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

}