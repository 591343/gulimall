package com.xiaochen.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mysql.cj.util.StringUtils;
import com.xiaochen.common.constant.ProductConstant;
import com.xiaochen.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.xiaochen.gulimall.product.dao.AttrGroupDao;
import com.xiaochen.gulimall.product.dao.CategoryDao;
import com.xiaochen.gulimall.product.entity.*;
import com.xiaochen.gulimall.product.service.ProductAttrValueService;
import com.xiaochen.gulimall.product.vo.AttrVo;
import com.xiaochen.gulimall.product.vo.respone.AttrRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.AttrDao;
import com.xiaochen.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryServiceImpl categoryService;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;

    @Autowired
    ProductAttrValueService productAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveDetail(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        this.save(attrEntity);

        if(attrVo.getAttrType().equals(ProductConstant.AttrEnum.ATTR_BASE.getCode())){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }


    @Transactional
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity=this.baseMapper.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,attrRespVo);
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrId));
        if(attrAttrgroupRelationEntity!=null)
            attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
        Long []paths=categoryService.findPath(attrRespVo.getCatelogId());
        attrRespVo.setCatelogPath(paths);
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);
        if(attr.getAttrType().equals(ProductConstant.AttrEnum.ATTR_BASE.getCode())) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne
                    (new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrAttrgroupRelationEntity != null) {
                attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attr.getAttrId()));
            }else{
                AttrAttrgroupRelationEntity attrAttrGroupRelationEntity=new AttrAttrgroupRelationEntity();
                attrAttrGroupRelationEntity.setAttrId(attr.getAttrId());
                attrAttrGroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationDao.insert(attrAttrGroupRelationEntity);
            }
        }

    }

    @Transactional
    @Override
    public PageUtils queryAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        String key= (String) params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type",attrType.equals("base")? ProductConstant.AttrEnum.ATTR_BASE.getCode():ProductConstant.AttrEnum.ATTR_SALE.getCode());
        if(catelogId!=0){
            wrapper.eq("catelog_id",catelogId);
        }
        if(!StringUtils.isEmptyOrWhitespaceOnly(key)){
            wrapper.and((attrEntityQueryWrapper -> {
                attrEntityQueryWrapper.eq("attr_id",key).or().like("attr_name",key);
            }));
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),wrapper);
        PageUtils pageUtils=new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> attrRespVos=records.stream().map((attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity,attrRespVo);
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=attrAttrgroupRelationDao.selectOne(new
                    QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrRespVo.getAttrId()));
            if(attrAttrgroupRelationEntity!=null){
                AttrGroupEntity attrGroupEntity=attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }


            CategoryEntity categoryEntity =categoryDao.selectById(attrRespVo.getCatelogId());
            if(categoryEntity!=null){
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        })).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Transactional
    @Override
    public PageUtils queryNoAttrPage(Map<String, Object> params, Long attrGroupId) {
        AttrGroupEntity attrGroupEntity=attrGroupDao.selectById(attrGroupId);

        QueryWrapper<AttrEntity> wrapper=new QueryWrapper<>();

        if(attrGroupEntity!=null){
            List<AttrEntity> attrEntityList=this.baseMapper.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id",attrGroupEntity.getCatelogId()));
            List<Long> attrIds=attrEntityList.stream().filter(item->{
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",item.getAttrId()));
                if(attrAttrgroupRelationEntity!=null){
                    return false;
                }
                //筛选出规格参数
                return item.getAttrType().equals(ProductConstant.AttrEnum.ATTR_BASE.getCode());
            }).map(AttrEntity::getAttrId).collect(Collectors.toList());
            System.out.println("mima");
            attrIds.forEach(System.out::println);

            String key=(String) params.get("key");
            if(!StringUtils.isEmptyOrWhitespaceOnly(key)){
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            }
            if(attrIds.size()>0) {
                wrapper.in("attr_id", attrIds);
            }else {
                IPage<AttrEntity> page = this.page(
                        new Query<AttrEntity>().getPage(params),new QueryWrapper<>()
                );
                page.setRecords(new ArrayList<>());
                return new PageUtils(page);
            }

        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> listForSpuAttr(Long spuId) {
        List<ProductAttrValueEntity> list=productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));

        return list;
    }

}