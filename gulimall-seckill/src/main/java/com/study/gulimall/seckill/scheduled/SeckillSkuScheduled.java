package com.study.gulimall.seckill.scheduled;


import com.study.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock="seckill:upload:lock:";

    @Scheduled(cron = "0,30 * * * * ?")
    //@Async
    public void uploadSeckillSkuLatest3Days(){
        log.info("shangjia");

        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.upLoadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }

}
