package com.xiaochen.gulimall.product.vo.respone;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CategoryBrandRelationRespVo {
    private static final long serialVersionUID = 1L;

    /**
     * Ʒ??id
     */
    private Long brandId;

    /**
     *
     */
    private String brandName;
}
