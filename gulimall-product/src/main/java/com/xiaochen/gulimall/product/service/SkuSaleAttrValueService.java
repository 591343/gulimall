package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku????????&ֵ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);


    /**
     * 通过spuId获取spu所有sku的销售属性
     * @param spuId
     * @return
     */
    List<SkuItemVo.SkuItemSaleEntity> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

