package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.SpuInfoEntity;
import com.xiaochen.gulimall.product.vo.request.SpuSaveVo;

import java.util.Map;

/**
 * spu??Ϣ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity vo);


    PageUtils queryPageByCondition(Map<String, Object> params);

    PageUtils spuInfoList(Map<String, Object> params);

    void skuUp(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

