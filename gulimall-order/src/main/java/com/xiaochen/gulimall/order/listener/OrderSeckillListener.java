package com.xiaochen.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xiaochen.common.to.mq.SecKillOrderTo;
import com.xiaochen.gulimall.order.entity.OrderEntity;
import com.xiaochen.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RabbitListener(queues = "${myRabbitmq.MQConfig.seckillQueue}")
public class OrderSeckillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrderTo, Channel channel, Message message) throws IOException {
        try {
            log.info("准备创建秒杀单的详细信息....");
            orderService.createSeckillOrder(secKillOrderTo);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
