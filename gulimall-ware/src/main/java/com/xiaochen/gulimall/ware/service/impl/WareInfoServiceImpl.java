package com.xiaochen.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.ware.feign.MemberFeignService;
import com.xiaochen.gulimall.ware.vo.respone.FareVo;
import com.xiaochen.gulimall.ware.vo.respone.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.ware.dao.WareInfoDao;
import com.xiaochen.gulimall.ware.entity.WareInfoEntity;
import com.xiaochen.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils wareInfoList(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().like("name",key).or().like("address",key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper

        );
        return new PageUtils(page);
    }

    @Override
    public FareVo computingFare(Long attrId) {
        MemberAddressVo memberAddressVo = memberFeignService.addr(attrId);
        FareVo fareVo=new FareVo();
        Random random = new Random();

        int i = random.nextInt(30);
        fareVo.setMemberAddressVo(memberAddressVo);
        fareVo.setFare(new BigDecimal(i+""));
        return fareVo;
    }

}