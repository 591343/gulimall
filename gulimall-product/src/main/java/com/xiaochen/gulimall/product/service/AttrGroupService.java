package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.vo.request.AttrGroupRelationVo;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import com.xiaochen.gulimall.product.vo.respone.WithAttrRespVo;

import java.util.List;
import java.util.Map;

/**
 * ???Է??
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrEntity> listAttrGroupRelation(Long attrGroupId);

    void removeRelationById(List<AttrGroupRelationVo> list);

    List<WithAttrRespVo> getWithAttr(Long cateLogId);

    List<SkuItemVo.SpuItemBaseAttrVo> selectSpuItemBaseAttrVo(Long catalogId, Long spuId);
}

