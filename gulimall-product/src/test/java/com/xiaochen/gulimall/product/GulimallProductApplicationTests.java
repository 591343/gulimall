package com.xiaochen.gulimall.product;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xiaochen.gulimall.product.dao.AttrGroupDao;
import com.xiaochen.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xiaochen.gulimall.product.service.CategoryService;
import com.xiaochen.gulimall.product.service.SkuInfoService;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import lombok.Data;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Autowired
    SkuInfoService skuInfoService;

    @Test
    public void test1() {
        Long[] a = categoryService.findPath(165L);
        System.out.println(Arrays.toString(Arrays.stream(a).toArray()));

    }

    @Test
    public void test2() {
        List<SkuItemVo.SpuItemBaseAttrVo> spuItemBaseAttrVos = attrGroupDao.selectItemBaseAttr(225L, 3L);
        for (SkuItemVo.SpuItemBaseAttrVo spuItemBaseAttrVo : spuItemBaseAttrVos) {
            System.out.println(spuItemBaseAttrVo);
        }
    }

    @Test
    public void test3() {
        List<SkuItemVo.SkuItemSaleEntity> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(3L);
        for (SkuItemVo.SkuItemSaleEntity skuItemSaleEntity : saleAttrBySpuId) {
            System.out.println(skuItemSaleEntity);
        }
    }

    @Test
    public void test4() throws ExecutionException, InterruptedException {
        SkuItemVo skuItemInfo = skuInfoService.getSkuItemInfo("1");
        System.out.println(skuItemInfo);
    }

    @Data
    @ToString
    static class User {
        private String username;  //用户名
        private String password;  //密码
    }




}