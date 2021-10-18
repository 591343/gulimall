package com.xiaochen.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.xiaochen.gulimall.product.vo.request.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaochen.gulimall.product.entity.SpuInfoEntity;
import com.xiaochen.gulimall.product.service.SpuInfoService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.R;



/**
 * spu??Ϣ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 20:51:25
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @GetMapping("/skuId/{id}")
    public R getSkuInfoBySkuId(@PathVariable("id") Long skuId){

        SpuInfoEntity entity = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(entity);
    }
    /**
     * spu检索
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.spuInfoList(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo){
		spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuSaveVo vo){
        //TODO 数据校验
        spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     *
     * 商品上架
     * @param spuId
     * @return
     */
    @PostMapping("/{spuId}/up")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@PathVariable("spuId") Long spuId){
        spuInfoService.skuUp(spuId);
        return R.ok();
    }
}
