package com.xiaochen.gulimall.product.feign;

import com.xiaochen.common.to.SkuReductionTo;
import com.xiaochen.common.to.SpuBoundTo;
import com.xiaochen.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
        //@RequiresPermissions("coupon:spubounds:save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction( @RequestBody SkuReductionTo skuReductionTo);
}
