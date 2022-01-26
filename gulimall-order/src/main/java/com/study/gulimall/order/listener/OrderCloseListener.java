package com.study.gulimall.order.listener;


import com.rabbitmq.client.Channel;
import com.study.common.to.mq.StockLockedTo;
import com.study.gulimall.order.entity.OrderEntity;
import com.study.gulimall.order.service.OrderService;
import com.study.gulimall.order.to.OrderCreatTo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁订单消息");
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("解锁成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
