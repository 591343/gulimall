package com.xiaochen.gulimall.product.web;

import com.xiaochen.gulimall.product.service.SkuInfoService;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") String skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo =skuInfoService.getSkuItemInfo(skuId);
        model.addAttribute("item",skuItemVo);

        return "item";
    }
}
