package com.study.gulimall.member.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.study.gulimall.product.dao")
public class MyBatisConfig {


//    @Bean
//    public PaginationInnerInterceptor paginationInnerInterceptor(){
//        PaginationInnerInterceptor paginationInnerInterceptor=new PaginationInnerInterceptor();
//
//        paginationInnerInterceptor.setOverflow(true);
//        paginationInnerInterceptor.setMaxLimit(1000l);
//
//        return  paginationInnerInterceptor;
//    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
