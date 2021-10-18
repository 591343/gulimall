package com.xiaochen.gulimall.member.feign;

import com.xiaochen.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponService {
    @RequestMapping("/coupon/coupon/membercoupons")
    public R memberCoupons();

}
