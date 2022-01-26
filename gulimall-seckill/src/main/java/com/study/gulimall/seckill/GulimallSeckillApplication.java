package com.study.gulimall.seckill;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients(basePackages = "com.study.gulimall.seckill.feign")
@SpringBootApplication
@EnableDiscoveryClient
@EnableRedisHttpSession
public class GulimallSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class,args);
    }
}
