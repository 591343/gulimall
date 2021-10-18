package com.xiaochen.gulimall.seckill.scheduled;


import com.xiaochen.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String  SECKILL_UPLOAD_LOCK="seckill:upload:lock";
    /**
     * 当天凌晨上架最近三天需要秒杀的物品，重复上架无需处理,幂等性操作，同一时间只有一个服务器执行上架任务,防止重复上架
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void upLoadSeckillSkuLatest3Days(){
        RLock lock = redissonClient.getLock(SECKILL_UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        log.info("秒杀商品正在上架...");
        try {
            seckillService.uploadSeckillSkuLatest3Day();
        }finally {
            //执行完成后必须释放锁
            lock.unlock();
        }
    }
}
