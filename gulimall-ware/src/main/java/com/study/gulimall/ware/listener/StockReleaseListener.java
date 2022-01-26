package com.study.gulimall.ware.listener;


import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.study.common.to.mq.StockDetailTo;
import com.study.common.to.mq.StockLockedTo;
import com.study.common.utils.R;
import com.study.common.vo.OrderVo;
import com.study.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.study.gulimall.ware.entity.WareOrderTaskEntity;
import com.study.gulimall.ware.service.WareSkuService;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存消息");

        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("解锁成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderVo orderVo, Message message, Channel channel) throws IOException {
        System.out.println("收到主动解锁库存消息");
        try {
            wareSkuService.unLockStock(orderVo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            System.out.println("解锁成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}
