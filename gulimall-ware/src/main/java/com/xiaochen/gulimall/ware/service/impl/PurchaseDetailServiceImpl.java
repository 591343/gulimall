package com.xiaochen.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.ware.dao.PurchaseDetailDao;
import com.xiaochen.gulimall.ware.entity.PurchaseDetailEntity;
import com.xiaochen.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key= (String) params.get("key");
        String status=(String) params.get("status");
        String wareId=(String) params.get("wareId");

        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
               w.eq("purchase_id",key).or().eq("sku_id",key);
            });
        }

        if(!StringUtils.isEmpty(status)){
            wrapper.and(w->{
                w.eq("status",status);
            });
        }

        if(!StringUtils.isEmpty(wareId)){
            wrapper.and(w->{
                w.eq("ware_id",wareId);
            });
        }


        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}