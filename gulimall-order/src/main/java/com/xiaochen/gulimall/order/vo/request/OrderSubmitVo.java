package com.xiaochen.gulimall.order.vo.request;

import com.xiaochen.gulimall.order.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId; //收货地址Id
    private Integer payType; //支付方式
    private String orderToken; //防重令牌
    private BigDecimal payPrice; //验价
    private String note; //订单备注
}
