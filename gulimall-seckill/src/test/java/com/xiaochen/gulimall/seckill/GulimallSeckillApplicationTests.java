package com.xiaochen.gulimall.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//@SpringBootTest
class GulimallSeckillApplicationTests {


    @Test
    public void test1(){
        System.out.println(getStartTime());
        System.out.println(getEndTime());

    }


    public String getStartTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String now = sdf.format(new Date().getTime());
        return now;
    }

    public String getEndTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.DAY_OF_YEAR,2);//日期加2天
        String end = sdf.format(rightNow.getTime());
        return end;
    }


}
