package com.xiaochen.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaochen.gulimall.product.entity.AttrEntity;
import lombok.Data;

@Data
public class AttrVo extends AttrEntity {
    private static final long serialVersionUID = 1L;


    /**
     * 属性分组ID
     */
    private Long attrGroupId;
}
