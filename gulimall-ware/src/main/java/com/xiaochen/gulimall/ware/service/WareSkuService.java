package com.xiaochen.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.to.HasStockTo;
import com.xiaochen.common.to.mq.OrderTo;
import com.xiaochen.common.to.mq.StockLockedTo;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.ware.entity.WareSkuEntity;
import com.xiaochen.gulimall.ware.vo.request.WareSkuLockVo;
import com.xiaochen.gulimall.ware.vo.respone.LockStockResult;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:15:26
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    void unlockStock(StockLockedTo to);

    PageUtils queryPage(Map<String, Object> params);

    List<HasStockTo> hasStock(List<Long> skuIds);

    Boolean lockOrderSku(WareSkuLockVo vo);

    /**
     * 由于订单超时而自动释放订单之后来解锁库存
     */
    void unlockStock(OrderTo to);
}

