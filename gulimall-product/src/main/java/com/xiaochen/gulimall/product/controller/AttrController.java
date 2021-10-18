package com.xiaochen.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xiaochen.gulimall.product.entity.ProductAttrValueEntity;
import com.xiaochen.gulimall.product.service.ProductAttrValueService;
import com.xiaochen.gulimall.product.vo.AttrVo;
import com.xiaochen.gulimall.product.vo.respone.AttrRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.service.AttrService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.R;



/**
 * ??Ʒ?
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 20:51:25
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;


    /**
     *
     * @param params
     * @param catelogId
     * @param attrType 表示属性类型，1表示规格参数，0表示销售属性，销售属性没有分组
     * @return
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId,@PathVariable("attrType") String attrType){

        PageUtils page=attrService.queryAttrPage(params,catelogId,attrType);
        return R.ok().put("page",page);
    }

    /**
     * url: /product/attr/base/listforspu/{spuId}
     * @param spuId
     * @return
     * 获取spu规格
     */
    @RequestMapping("/base/listforspu/{spuId}")
    //@RequiresPermissions("product:attr:list")
    public R list(@PathVariable("spuId") Long spuId){
         List<ProductAttrValueEntity> list = attrService.listForSpuAttr(spuId);

        return R.ok().put("data", list);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo attr = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */

    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveDetail(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**修改商品规格
     * /product/attr/update/{spuId}
     * @param list
     * @return
     */
    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R update(@PathVariable("spuId") Long spuId,@RequestBody List<ProductAttrValueEntity> list ){
        productAttrValueService.updateProductAttr(spuId,list);
        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
        //TODO规格参数删除时要级联删除自身，和属性组和属性关系表
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
