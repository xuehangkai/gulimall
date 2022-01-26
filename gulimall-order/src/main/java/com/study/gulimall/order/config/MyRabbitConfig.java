package com.study.gulimall.order.config;


import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Jsr330ScopeMetadataResolver;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    //@Autowired
    //RabbitTemplate rabbitTemplate;

//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(messageConverter());
//        return rabbitTemplate;
//    }





    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

//    @PostConstruct
//    public void initRabbitTemplate(){
//
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            @Override
//            public void confirm(CorrelationData correlationData, boolean b, String s) {
//                System.out.println(correlationData);
//                System.out.println(b);
//                System.out.println(s);
//            }
//        });
//
//        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
//            @Override
//            public void returnedMessage(ReturnedMessage returnedMessage) {
//                System.out.println(returnedMessage);
//            }
//        });
//
//    }
}
