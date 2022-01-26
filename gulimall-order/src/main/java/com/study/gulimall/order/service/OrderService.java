package com.study.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.to.mq.SeckillOrderTo;
import com.study.common.utils.PageUtils;
import com.study.gulimall.order.entity.OrderEntity;
import com.study.gulimall.order.to.OrderCreatTo;
import com.study.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:12:39
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo payAsyncVo);

    void creatSeckillOrder(SeckillOrderTo seckillOrderTo);
}

