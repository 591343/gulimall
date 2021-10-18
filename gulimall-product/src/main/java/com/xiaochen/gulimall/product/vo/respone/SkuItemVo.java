package com.xiaochen.gulimall.product.vo.respone;

import com.xiaochen.gulimall.product.entity.SkuImagesEntity;
import com.xiaochen.gulimall.product.entity.SkuInfoEntity;
import com.xiaochen.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品详情
 */
@Data
public class SkuItemVo {
    SkuInfoEntity info;

    Boolean hasStock =true;

    List<SkuImagesEntity> images;

    List<SkuItemSaleEntity> saleAttr;

    SpuInfoDescEntity desc;

    List<SpuItemBaseAttrVo> groupAttrs;

    /**
     * 秒杀信息
     */
    SeckillInfoVo seckillInfoVo;
    @Data
    public static class SkuItemSaleEntity{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class SpuItemBaseAttrVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;

    }

    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }


}
