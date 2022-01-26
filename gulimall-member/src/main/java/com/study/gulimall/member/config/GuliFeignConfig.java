package com.study.gulimall.member.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {


    @Bean
    public RequestInterceptor requestInterceptor(){
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    if (request!=null){
                        requestTemplate.header("Cookie",request.getHeader("Cookie"));
                    }
                }
            }
        };

        return requestInterceptor;
    }

}
