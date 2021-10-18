package com.xiaochen.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.ProductAttrValueDao;
import com.xiaochen.gulimall.product.entity.ProductAttrValueEntity;
import com.xiaochen.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        this.saveBatch(collect);
    }
    @Transactional
    @Override
    public void updateProductAttr(Long spuId, List<ProductAttrValueEntity> list) {

        if(list!=null) {
            this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

            this.saveBatch(list.stream().peek(item -> item.setSpuId(spuId)).collect(Collectors.toList()));
        }
    }

    @Override
    public List<ProductAttrValueEntity> getProductAttrValueList(Long spuId) {
        return this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
    }
}