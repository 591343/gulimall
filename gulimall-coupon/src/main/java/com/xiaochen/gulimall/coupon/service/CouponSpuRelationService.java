package com.xiaochen.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * ?Ż?ȯ????Ʒ????
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:10:07
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

