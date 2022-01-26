package com.study.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.to.SkuHasStockVo;
import com.study.common.to.mq.StockLockedTo;
import com.study.common.utils.PageUtils;
import com.study.common.vo.OrderVo;
import com.study.gulimall.ware.entity.WareSkuEntity;

import com.study.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:13:32
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);


    void unLockStock(StockLockedTo to);

    void unLockStock(OrderVo orderVo);
}

