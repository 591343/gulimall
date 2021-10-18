package com.xiaochen.gulimall.product.dao;

import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ???Է??
 * 
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SkuItemVo.SpuItemBaseAttrVo> selectItemBaseAttr(@Param("catalogId") Long catalogId, @Param("spuId") Long spuId);
}
