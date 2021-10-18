package com.xiaochen.common.constant;

import lombok.Getter;

public class ProductConstant {
    @Getter
     public enum AttrEnum{
        ATTR_BASE(1,"规格参数"),ATTR_SALE(0,"销售属性");
        private Integer code;
        private String message;
        AttrEnum(Integer code,String message){
            this.code=code;
            this.message=message;
        }
    }

    @Getter
    public enum AttrSearchTypeEnum{
        ATTR_ENABLE_SEARCH(1,"规格属性可以被检索"),ATTR_UNABLE_SEARCH(0,"属性不可被检索");
        private Integer code;
        private String message;
        AttrSearchTypeEnum(Integer code,String message){
            this.code=code;
            this.message=message;
        }
    }

    @Getter
    public enum StatsEnum{
        NEW_SPU(0,"新建"),
        SPU_UP(1,"上架"),
        SPU_DOWN(2,"下架");
        private Integer code;
        private String message;
        StatsEnum(Integer code,String message){
            this.code=code;
            this.message=message;
        }
    }
}
