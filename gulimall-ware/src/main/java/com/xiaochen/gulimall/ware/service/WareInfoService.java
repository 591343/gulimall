package com.xiaochen.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.ware.entity.WareInfoEntity;
import com.xiaochen.gulimall.ware.vo.respone.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:15:26
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils wareInfoList(Map<String, Object> params);

    FareVo computingFare(Long attrId);
}

