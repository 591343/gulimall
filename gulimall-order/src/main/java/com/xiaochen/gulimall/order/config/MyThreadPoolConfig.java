package com.xiaochen.gulimall.order.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool){
        return new ThreadPoolExecutor(pool.getCorePoolSize(),pool.getMaximumPoolSize(), pool.getKeepAliveTime(), TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(100000),Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
    }
}
