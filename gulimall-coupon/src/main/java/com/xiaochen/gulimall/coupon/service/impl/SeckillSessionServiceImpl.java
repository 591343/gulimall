package com.xiaochen.gulimall.coupon.service.impl;

import com.xiaochen.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.xiaochen.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.coupon.dao.SeckillSessionDao;
import com.xiaochen.gulimall.coupon.entity.SeckillSessionEntity;
import com.xiaochen.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        String startTime = getStartTime();
        String endTime = getEndTime();
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime, endTime));
        if(list!=null&&list.size()!=0){
            List<SeckillSessionEntity> collect = list.stream().map(item -> {
                Long id = item.getId();
                List<SeckillSkuRelationEntity> skus = seckillSkuRelationService.getSessionSkusById(id);
                item.setRelationSkus(skus);
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    private String getStartTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String now = sdf.format(new Date().getTime());
        return now;
    }

    private String getEndTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.DAY_OF_YEAR,2);//日期加2天
        String end = sdf.format(rightNow.getTime());
        return end;
    }

}