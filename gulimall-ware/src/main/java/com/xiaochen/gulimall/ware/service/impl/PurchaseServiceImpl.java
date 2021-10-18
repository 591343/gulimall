package com.xiaochen.gulimall.ware.service.impl;

import com.xiaochen.common.constant.WareConstant;
import com.xiaochen.gulimall.ware.entity.PurchaseDetailEntity;
import com.xiaochen.gulimall.ware.service.PurchaseDetailService;
import com.xiaochen.gulimall.ware.vo.request.PurchaseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.ware.dao.PurchaseDao;
import com.xiaochen.gulimall.ware.entity.PurchaseEntity;
import com.xiaochen.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils unReceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void purchaseMerge(PurchaseVo vo) {
        Long purchaseId=vo.getPurchaseId();
        if(purchaseId==null){
            PurchaseEntity purchaseEntity=new PurchaseEntity();
            purchaseEntity.setPriority(1);
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date(new java.util.Date().getTime()));
            purchaseEntity.setUpdateTime(new Date(new java.util.Date().getTime()));
            this.save(purchaseEntity);
            purchaseId=purchaseEntity.getId();
        }

        List<Long> items=vo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities=items.stream().map(item->{
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity=new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date(new java.util.Date().getTime()));

        this.updateById(purchaseEntity);
    }


}