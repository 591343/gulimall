package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.entity.ProductAttrValueEntity;
import com.xiaochen.gulimall.product.vo.AttrVo;
import com.xiaochen.gulimall.product.vo.respone.AttrRespVo;

import java.util.List;
import java.util.Map;

/**
 * ??Ʒ?
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(AttrVo attrVo);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    PageUtils queryNoAttrPage(Map<String, Object> params, Long attrGroupId);

    List<ProductAttrValueEntity> listForSpuAttr(Long spuId);

}

