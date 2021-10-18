package com.xiaochen.gulimall.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
