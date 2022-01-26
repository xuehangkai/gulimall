package com.study.gulimall.auth;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.study.gulimall.auth.feign")
public class GulimallAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthApplication.class, args);
    }

    //@Bean
    public LettuceConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory();
    }
}
