package com.xiaochen.gulimall.product.service.impl;

import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xiaochen.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.xiaochen.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.SkuItemSaleEntity> getSaleAttrBySpuId(Long spuId) {
        SkuSaleAttrValueDao baseMapper = this.baseMapper;
        List<SkuItemVo.SkuItemSaleEntity> saleAttr=baseMapper.getSaleAttrBySpuId(spuId);
        return saleAttr;
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        List<String> data=skuSaleAttrValueDao.getSkuSaleAttrValues(skuId);
        return data;
    }


}