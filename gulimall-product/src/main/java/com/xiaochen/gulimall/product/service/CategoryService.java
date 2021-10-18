package com.xiaochen.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.entity.CategoryEntity;
import com.xiaochen.gulimall.product.vo.respone.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * ??Ʒ???????
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listTree();

    void removeCategoryByIds(Long[] catIds);

    Long [] findPath(Long cateLogId);

    void updateCascade(CategoryEntity category);

    Map<String, List<Catalog2Vo>> getCatalogs();
}

