package com.xiaochen.gulimall.order.feign;

import com.xiaochen.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@FeignClient("gulimall-product")
@RequestMapping("/product/spuinfo")
public interface ProductFeignService {
    @GetMapping("/skuId/{id}")
    R getSkuInfoBySkuId(@PathVariable("id") Long skuId);
}
