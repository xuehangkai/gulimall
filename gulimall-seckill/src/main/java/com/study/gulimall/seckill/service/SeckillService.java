package com.study.gulimall.seckill.service;

import com.study.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeckillService {


    void upLoadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String seckill(String killId, String key, Integer num);
}
