package com.xiaochen.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.to.mq.SecKillOrderTo;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.order.entity.OrderEntity;
import com.xiaochen.gulimall.order.vo.OrderConfirmVo;
import com.xiaochen.gulimall.order.vo.request.OrderSubmitVo;
import com.xiaochen.gulimall.order.vo.response.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * ????
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:14:14
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity entity);

    void createSeckillOrder(SecKillOrderTo secKillOrderTo);
}

