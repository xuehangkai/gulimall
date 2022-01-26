package com.study.gulimall.ware.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

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
