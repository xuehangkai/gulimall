package com.study.gulimall.ware.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {

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

    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Queue stockDelayQueue(){
        Map<String ,Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release.stock");
        arguments.put("x-message-ttl",120000L);
        Queue queue = new Queue("stock.delay.queue", true, false, false,arguments);
        return queue;
    }
    @Bean
    public Queue stockReleaseOrderQueue(){
        Queue queue = new Queue("stock.release.stock.queue", true, false, false);
        return queue;
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("stock.delay.queue",Binding.DestinationType.QUEUE,"stock-event-exchange","stock.locked",null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("stock.release.stock.queue",Binding.DestinationType.QUEUE,"stock-event-exchange","stock.release.#",null);
    }
}
