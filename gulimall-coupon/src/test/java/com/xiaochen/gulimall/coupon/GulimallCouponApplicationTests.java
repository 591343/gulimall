package com.xiaochen.gulimall.coupon;

import com.xiaochen.gulimall.coupon.entity.SeckillSessionEntity;
import com.xiaochen.gulimall.coupon.service.SeckillSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallCouponApplicationTests {
    @Autowired
    SeckillSessionService seckillSessionService;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
        List<SeckillSessionEntity> latest3DaySession = seckillSessionService.getLatest3DaySession();
        for (SeckillSessionEntity seckillSessionEntity : latest3DaySession) {
            System.out.println(seckillSessionEntity);
        }
    }
}
