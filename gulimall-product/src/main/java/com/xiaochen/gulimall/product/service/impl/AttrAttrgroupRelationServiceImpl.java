package com.xiaochen.gulimall.product.service.impl;

import com.xiaochen.gulimall.product.vo.request.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.xiaochen.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xiaochen.gulimall.product.service.AttrAttrgroupRelationService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveBatch(List<AttrGroupRelationVo> list) {
        List<AttrAttrgroupRelationEntity> entities=list.stream().map(item->{
            AttrAttrgroupRelationEntity entity=new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item,entity);
            return entity;
        }).collect(Collectors.toList());
        this.saveBatch(entities);
    }

}