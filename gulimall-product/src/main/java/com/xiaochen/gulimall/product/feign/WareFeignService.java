package com.xiaochen.gulimall.product.feign;


import com.xiaochen.common.to.HasStockTo;
import com.xiaochen.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 检查skus是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R hasStock(@RequestBody List<Long> skuIds);
}
