package com.xiaochen.common.constant;

import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

/**
 * 仓储服务常量
 */
public class WareConstant {
    @Getter
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        RECEIVED(2,"已领取"),FINISHED(3,"已完成"),
        EXCEPTED(4,"有异常");
        private Integer code;
        private String message;
        PurchaseStatusEnum(Integer code,String message){
            this.code=code;
            this.message=message;
        }
    }


    @Getter
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),
        PURCHASING(2,"正在采购"),FINISHED(3,"已完成"),
        FAILED(4,"采购失败");
        private Integer code;
        private String message;
        PurchaseDetailStatusEnum(Integer code,String message){
            this.code=code;
            this.message=message;
        }
    }
}
