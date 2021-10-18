package com.xiaochen.gulimall.product.vo.respone;

import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.vo.AttrVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 获取分类下所有分组&关联属性专用Vo
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WithAttrRespVo extends AttrGroupEntity {
    List<AttrVo> attrs;  //属性集合
}
