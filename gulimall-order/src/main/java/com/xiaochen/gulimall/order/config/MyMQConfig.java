package com.xiaochen.gulimall.order.config;




import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {
    @Value("${myRabbitmq.MQConfig.queues}")
    private String queues;

    @Value("${myRabbitmq.MQConfig.eventExchange}")
    private String eventExchange;

    @Value("${myRabbitmq.MQConfig.routingKey}")
    private String routingKey;

    @Value("${myRabbitmq.MQConfig.delayQueue}")
    private String delayQueue;

    @Value("${myRabbitmq.MQConfig.createOrder}")
    private String createOrder;

    @Value("${myRabbitmq.MQConfig.ReleaseOther}")
    private String ReleaseOther;

    @Value("${myRabbitmq.MQConfig.ReleaseOtherKey}")
    private String ReleaseOtherKey;

    @Value("${myRabbitmq.MQConfig.seckillOrderKey}")
    private String seckillOrderKey;

    @Value("${myRabbitmq.MQConfig.ttl}")
    private Integer ttl;

    @Value("${myRabbitmq.MQConfig.seckillQueue}")
    private String seckillQueue;


    /**
     * String name, boolean durable, boolean exclusive, boolean autoDelete,  @Nullable Map<String, Object> arguments
     */
    @Bean
    public Queue orderDelayQueue(){
        System.out.println("创建orderDelayQueueBean");
        Map<String ,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", eventExchange);
        arguments.put("x-dead-letter-routing-key", routingKey);
        arguments.put("x-message-ttl", ttl);
        Queue queue = new Queue(delayQueue, true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue(queues, true, false, false);
        return queue;
    }

    @Bean
    public Queue orderSeckillOrderQueue(){
        Queue queue = new Queue(seckillQueue, true, false, false);
        return queue;
    }

    /**
     * String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){

        return new TopicExchange(eventExchange, true, false);
    }

    /**
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding orderCreateOrderBinding(){

        return new Binding(delayQueue, Binding.DestinationType.QUEUE, eventExchange, createOrder, null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){

        return new Binding(queues, Binding.DestinationType.QUEUE, eventExchange, routingKey, null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     */
    @Bean
    public Binding orderReleaseOtherBinding(){

        return new Binding(ReleaseOther, Binding.DestinationType.QUEUE, eventExchange, ReleaseOtherKey + ".#", null);
    }

    @Bean
    public Binding orderSeckillOrderBinding(){

        return new Binding(seckillQueue, Binding.DestinationType.QUEUE, eventExchange, seckillOrderKey, null);
    }
}
