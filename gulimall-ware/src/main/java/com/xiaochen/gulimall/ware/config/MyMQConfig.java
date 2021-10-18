package com.xiaochen.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {
    @Value("${myRabbitmq.MQConfig.queues}")
    private  String queues;

    @Value("${myRabbitmq.MQConfig.eventExchange}")
    private  String eventExchange;

    @Value("${myRabbitmq.MQConfig.routingKey}")
    private  String routingKey;

    @Value("${myRabbitmq.MQConfig.delayQueue}")
    private  String delayQueue;

    @Value("${myRabbitmq.MQConfig.letterRoutingKey}")
    private  String letterRoutingKey;

    @Value("${myRabbitmq.MQConfig.ttl}")
    private  Integer ttl;

    private final String releaseQueue=queues;

    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
     */
    @Bean
    public Exchange stockEventExchange(){

        return new TopicExchange(eventExchange, true, false);
    }

    /**
     * String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Queue stockReleaseQueue(){
        return new Queue(queues, true, false, false);
    }

    @Bean
    public Queue stockDelayQueue(){

        Map<String, Object> args = new HashMap<>();
        // 信死了 交给 [stock-event-exchange] 交换机
        args.put("x-dead-letter-exchange",eventExchange);
        // 死信路由
        args.put("x-dead-letter-routing-key",letterRoutingKey);
        args.put("x-message-ttl", ttl);

        return new Queue(delayQueue, true, false, false, args);
    }

    /**
     * 普通队列的绑定关系
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding stockLockedReleaseBinding(){

        return new Binding(queues,Binding.DestinationType.QUEUE,eventExchange,letterRoutingKey + ".#", null);
    }

    /**
     * 延时队列的绑定关系
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding stockLockedBinding(){

        return new Binding(delayQueue, Binding.DestinationType.QUEUE, eventExchange, routingKey, null);
    }

    @RabbitListener(queues = "stock.release.stock.queue")
    public void handle(Message message){


    }

}
