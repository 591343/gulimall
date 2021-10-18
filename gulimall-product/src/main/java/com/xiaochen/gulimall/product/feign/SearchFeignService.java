package com.xiaochen.gulimall.product.feign;

import com.xiaochen.common.to.es.SkuEsModel;
import com.xiaochen.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/skuproduct")
    R saveSkuProduct(@RequestBody List<SkuEsModel> skuEsModels);
}
