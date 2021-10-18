package com.xiaochen.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xiaochen.common.exception.BizCodeEnum;
import com.xiaochen.common.exception.NotStockException;
import com.xiaochen.common.to.HasStockTo;
import com.xiaochen.gulimall.ware.vo.request.WareSkuLockVo;
import com.xiaochen.gulimall.ware.vo.respone.LockStockResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaochen.gulimall.ware.entity.WareSkuEntity;
import com.xiaochen.gulimall.ware.service.WareSkuService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.R;



/**
 * 商品库存
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:15:26
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 对订单中的商品进行锁库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    public R lockOrderSku(@RequestBody WareSkuLockVo vo){
        try {
            Boolean lockSuccess = wareSkuService.lockOrderSku(vo);
            return R.ok();
        }catch (NotStockException e){
            return R.error(BizCodeEnum.NOT_STOCK_EXCEPTION.getCode(),BizCodeEnum.NOT_STOCK_EXCEPTION.getMessage());
        }
    }


    /**
     * 检查skus是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/hasstock")
    //@RequiresPermissions("ware:waresku:list")
    public R hasStock(@RequestBody List<Long> skuIds){
        List<HasStockTo> hasStockTos = wareSkuService.hasStock(skuIds);
        return R.ok().put("data",hasStockTos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
