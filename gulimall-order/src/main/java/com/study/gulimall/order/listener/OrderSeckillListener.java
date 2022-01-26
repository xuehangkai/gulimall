package com.study.gulimall.order.listener;


import com.rabbitmq.client.Channel;
import com.study.common.to.mq.SeckillOrderTo;
import com.study.gulimall.order.entity.OrderEntity;
import com.study.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RabbitListener(queues = {"order.seckill.order.queue"})
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        System.out.println("收到秒杀订单消息");
        try {
            orderService.creatSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("秒杀订单创建成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
