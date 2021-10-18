package com.xiaochen.gulimall.product.vo.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 3级分类Vo
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog3Vo {
    private String catalog2Id;
    private String id;
    private String name;
}
