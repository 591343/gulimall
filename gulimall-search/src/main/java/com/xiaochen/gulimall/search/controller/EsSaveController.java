package com.xiaochen.gulimall.search.controller;


import com.xiaochen.common.exception.BizCodeEnum;
import com.xiaochen.common.to.es.SkuEsModel;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/search/save")
@RestController
public class EsSaveController {

    @Autowired
    ProductSaveService productSaveService;

    /**
     * 向ES中保存商品上架信息
     * @param skuEsModels
     * @return
     */
    @PostMapping("/skuproduct")
    public R saveSkuProduct(@RequestBody List<SkuEsModel> skuEsModels){
        Boolean hasFailure=false;
        try {
            hasFailure=productSaveService.productStatsUp(skuEsModels);
        } catch (IOException e) {
            R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if(hasFailure){
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        return R.ok();
    }

}
