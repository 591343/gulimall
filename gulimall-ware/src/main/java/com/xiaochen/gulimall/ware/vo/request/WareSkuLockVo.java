package com.xiaochen.gulimall.ware.vo.request;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn; //订单号
    private List<OrderItemVo> locks; //待锁商品
}
