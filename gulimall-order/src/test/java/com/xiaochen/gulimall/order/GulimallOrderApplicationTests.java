package com.xiaochen.gulimall.order;

import com.xiaochen.gulimall.order.entity.OrderItemEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {


    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    void contextLoads() {
    }

    @Test
    void sendMessageTest(){
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderId(1L);
        orderItemEntity.setSkuName("华为手机");
        String message="hello world";
        rabbitTemplate.convertAndSend("ustb.direct","jty.news",orderItemEntity);
        log.info("{}消息发送成功",message);
    }
    @Test
    void test1(){

        amqpAdmin.declareExchange(new DirectExchange("ustb.direct",true,false));  //创建直接交换机
        log.info("Exchange{}创建成功","ustb.direct");
        amqpAdmin.declareQueue(new Queue("ustb.news"));
        log.info("Queue{}创建成功","ustb.news");
        amqpAdmin.declareBinding(new Binding("ustb.news", Binding.DestinationType.QUEUE,"ustb.direct","jty.news",null));
        log.info("Binding{}创建成功","binding1");
    }

}
