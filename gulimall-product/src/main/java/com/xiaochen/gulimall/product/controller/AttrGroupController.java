package com.xiaochen.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xiaochen.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.service.AttrAttrgroupRelationService;
import com.xiaochen.gulimall.product.service.AttrService;
import com.xiaochen.gulimall.product.service.CategoryService;
import com.xiaochen.gulimall.product.vo.request.AttrGroupRelationVo;
import com.xiaochen.gulimall.product.vo.respone.WithAttrRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.service.AttrGroupService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.R;



/**
 * 属性分组
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 20:51:25
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 获取属性分组的关联的所有属性
     * @param attrGroupId
     * @return
     */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R listAttrGroup(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list=attrGroupService.listAttrGroupRelation(attrGroupId);
        return R.ok().put("data",list);
    }

    ///product/attrgroup/{attrgroupId}/noattr/relation

    @GetMapping("{attrgroupId}/noattr/relation")
    public R listNoAttrRelation(@RequestParam  Map<String, Object> params,@PathVariable("attrgroupId") Long attrGroupId){

        PageUtils page= attrService.queryNoAttrPage(params,attrGroupId);
        return R.ok().put("page",page);
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		Long [] catePath=categoryService.findPath(attrGroup.getCatelogId());
		attrGroup.setCatePath(catePath);

        return R.ok().put("attrGroup", attrGroup);
    }


    /**
     * 获取分类下所有分组&关联属性
     * @param cateLogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R listWithAttr(@PathVariable("catelogId") Long cateLogId){
        List<WithAttrRespVo> withAttrRespVos=attrGroupService.getWithAttr(cateLogId);

        return R.ok().put("data",withAttrRespVos);
    }
    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    @PostMapping("/attr/relation")
    public R attrRelationSave(@RequestBody List<AttrGroupRelationVo> list){
        attrAttrgroupRelationService.saveBatch(list);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody List<AttrGroupRelationVo> list){
        attrGroupService.removeRelationById(list);
        return R.ok();
    }

}
