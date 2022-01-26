package com.study.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@MapperScan("com.study.gulimall.order.dao")
@EnableDiscoveryClient
@EnableRabbit
@EnableFeignClients(basePackages = "com.study.gulimall.order.feign")
@EnableRedisHttpSession
@EnableAspectJAutoProxy(exposeProxy = true)
public class GulimallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class,args);
    }
}
