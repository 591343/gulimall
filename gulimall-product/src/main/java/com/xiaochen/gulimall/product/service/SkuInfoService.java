package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.SkuInfoEntity;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku??Ϣ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils skuInfoList(Map<String, Object> params);

    /**
     * 通过SpuId获取所有的Sku信息
     * @return
     */
    List<SkuInfoEntity> getSkusById(Long spuId);


    /**
     * 通过商品skuId进行商品详情查询
     * @param skuId
     * @return
     */
    SkuItemVo getSkuItemInfo(String skuId) throws ExecutionException, InterruptedException;
}

