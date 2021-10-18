package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.SkuImagesEntity;

import java.util.Map;

/**
 * skuͼƬ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

