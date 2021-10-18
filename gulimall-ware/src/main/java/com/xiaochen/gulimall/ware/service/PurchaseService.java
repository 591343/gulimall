package com.xiaochen.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.ware.entity.PurchaseEntity;
import com.xiaochen.gulimall.ware.vo.request.PurchaseVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:15:26
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils unReceiveList(Map<String, Object> params);

    void purchaseMerge(PurchaseVo vo);
}

