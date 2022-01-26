package com.study.gulimall.ware.dao;

import com.study.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:13:32
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
