package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu????ֵ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
    void saveProductAttr(List<ProductAttrValueEntity> collect);

    void updateProductAttr(Long spuId, List<ProductAttrValueEntity> list);

    List<ProductAttrValueEntity> getProductAttrValueList(Long spuId);
}

