package com.xiaochen.gulimall.product.service.impl;

import com.mysql.cj.util.StringUtils;
import com.xiaochen.gulimall.product.entity.CategoryBrandRelationEntity;
import com.xiaochen.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.BrandDao;
import com.xiaochen.gulimall.product.entity.BrandEntity;
import com.xiaochen.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key= (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper=new QueryWrapper<>();
        if(!StringUtils.isEmptyOrWhitespaceOnly(key)){
            queryWrapper.eq("brand_id",key).or().like("name",key);
            log.debug("进来了");
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),queryWrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
            //保证荣誉字段的数据一致
        this.baseMapper.updateById(brand);
        if(!StringUtils.isEmptyOrWhitespaceOnly(brand.getName())){
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
        }

        //TODO 更新其他关联
    }

}