package com.xiaochen.gulimall.order.vo.request;

import com.xiaochen.gulimall.order.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    MemberAddressVo memberAddressVo;
    BigDecimal fare;
}
