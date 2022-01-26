package com.study.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.common.to.mq.SeckillOrderTo;
import com.study.common.utils.R;
import com.study.common.vo.MemberEntityVo;
import com.study.gulimall.seckill.feign.CouponFeignService;
import com.study.gulimall.seckill.feign.ProductFeignService;
import com.study.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.study.gulimall.seckill.service.SeckillService;
import com.study.gulimall.seckill.to.SeckillSkuRedisTo;
import com.study.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.study.gulimall.seckill.vo.SeckillsSkuVo;
import com.study.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckilServiceImpl implements SeckillService {


    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void upLoadSeckillSkuLatest3Days() {
        R session = couponFeignService.getLates3DaySession();

        if (session.getCode() == 0) {
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            saveSessionInfos(sessionData);
            saveSessionSkuInfos(sessionData);


        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);

                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                        //seckillSkuRedisTo.setRandomCode(null);
                        return seckillSkuRedisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    long current = new Date().getTime();
                    Long startTime = seckillSkuRedisTo.getStartTime();
                    Long endTime = seckillSkuRedisTo.getEndTime();
                    if (current >= startTime && current <= endTime) {

                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String seckill(String killId, String key, Integer num) {
        MemberEntityVo memberEntityVo = LoginUserInterceptor.toThreadLocal.get();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            Long ttl = endTime - startTime;
            if (new Date().getTime() >= startTime && new Date().getTime() <= endTime) {
                String randomCode = seckillSkuRedisTo.getRandomCode();
                String id = seckillSkuRedisTo.getPromotionSessionId() + "_" + seckillSkuRedisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(id)) {
                    if (num <= seckillSkuRedisTo.getSeckillLimit().intValue()) {
                        String redisKey = memberEntityVo.getId() + "_" + id;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                                boolean b = semaphore.tryAcquire(num);
                                if (b) {
                                    String s1 = UUID.randomUUID().toString();
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(s1);
                                    seckillOrderTo.setMemberId(memberEntityVo.getId());
                                    seckillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                                    seckillOrderTo.setNum(num);
                                    seckillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                                    seckillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                                    rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                    return s1;
                                }
                                return null;
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }

            } else {
                return null;
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> collect = session.getRelationSkus().stream().map(item -> {
                    return item.getPromotionSessionId() + "_" + item.getSkuId().toString();
                }).collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillsSkuVo -> {
                String token = UUID.randomUUID().toString().replace("-", "");
                if (!hashOps.hasKey(seckillsSkuVo.getPromotionSessionId().toString() + "_" + seckillsSkuVo.getSkuId().toString())) {
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    R skuInfo = productFeignService.getSkuInfo(seckillsSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuRedisTo.setSkuInfoVo(info);
                    }
                    BeanUtils.copyProperties(seckillsSkuVo, seckillSkuRedisTo);
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());
                    seckillSkuRedisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(seckillSkuRedisTo);
                    hashOps.put(seckillsSkuVo.getPromotionSessionId().toString() + "_" + seckillsSkuVo.getSkuId().toString(), jsonString);

                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillsSkuVo.getSeckillCount());
                }
            });
        });
    }
}
