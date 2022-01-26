package com.study.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.study.common.to.SkuHasStockVo;
import com.study.common.to.mq.StockDetailTo;
import com.study.common.to.mq.StockLockedTo;
import com.study.common.utils.R;
import com.study.common.vo.OrderVo;
import com.study.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.study.gulimall.ware.entity.WareOrderTaskEntity;
import com.study.gulimall.ware.exception.NoStockException;
import com.study.gulimall.ware.feign.OrderFeignService;
import com.study.gulimall.ware.feign.ProductFeignService;
import com.study.gulimall.ware.service.WareOrderTaskDetailService;
import com.study.gulimall.ware.service.WareOrderTaskService;
import com.study.gulimall.ware.vo.OrderItemVo;

import com.study.gulimall.ware.vo.WareSkuLockVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.ware.dao.WareSkuDao;
import com.study.gulimall.ware.entity.WareSkuEntity;
import com.study.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;


    @Transactional
    public void unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        wareSkuDao.unLockStock(skuId, wareId, num);
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(detailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        System.out.println(skuId + "--" + wareId + "--" + skuNum);

        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            wareSkuEntity.setStockLocked(0);
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);

            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;

        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo vo) {
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {

                }
            }

            if (skuStocked == false) {
                throw new NoStockException(skuId);
            }

        }
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {

        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    if(byId.getLockStatus()==1){
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                throw new RuntimeException("远程服务失败");
            }
        } else {

        }
    }

    @Override
    @Transactional
    public void unLockStock(OrderVo orderVo) {
        String orderSn = orderVo.getOrderSn();
        WareOrderTaskEntity task= wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = task.getId();
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status",1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}