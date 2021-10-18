package com.xiaochen.gulimall.ware.vo.respone;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    MemberAddressVo memberAddressVo;
    BigDecimal fare;
}
