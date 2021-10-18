package com.xiaochen.gulimall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.log.Log;
import com.mysql.cj.util.StringUtils;
import com.xiaochen.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.xiaochen.gulimall.product.dao.AttrDao;
import com.xiaochen.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xiaochen.gulimall.product.entity.AttrEntity;
import com.xiaochen.gulimall.product.vo.AttrVo;
import com.xiaochen.gulimall.product.vo.request.AttrGroupRelationVo;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import com.xiaochen.gulimall.product.vo.respone.WithAttrRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.AttrGroupDao;
import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrDao attrDao;


    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper=new QueryWrapper<>();

        if(catelogId==0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper.and(attrGroupEntityQueryWrapper -> attrGroupEntityQueryWrapper.eq("attr_group_id",key).or().like("attr_group_name",key)));
            return new PageUtils(page);
        }else {
            if(!StringUtils.isNullOrEmpty(key)){
                wrapper.eq("catelog_id",catelogId).and(attrGroupEntityQueryWrapper -> attrGroupEntityQueryWrapper.eq("attr_group_id",key).or().like("attr_group_name",key));
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                   wrapper);
            return new PageUtils(page);
        }
    }


    @Transactional
    @Override
    public List<AttrEntity> listAttrGroupRelation(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> list=attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",attrGroupId));
        return list.stream().map(item->attrDao.selectById(item.getAttrId())).collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void removeRelationById(List<AttrGroupRelationVo> list) {
        for(AttrGroupRelationVo attrGroupRelationVo:list){
            attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id",attrGroupRelationVo.getAttrId())
                    .eq("attr_group_id",attrGroupRelationVo.getAttrGroupId()));
        }
    }

    @Transactional
    @Override
    public List<WithAttrRespVo> getWithAttr(Long cateLogId) {
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id",cateLogId));
        List<WithAttrRespVo> withAttrRespVos=attrGroupEntities.stream().map(item->{
            WithAttrRespVo withAttrRespVo = new WithAttrRespVo();
            List<AttrAttrgroupRelationEntity> entities=attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",item.getAttrGroupId()));
            List<AttrVo> attrVoList=entities.stream().map(obj->{
                AttrEntity attrEntity = attrDao.selectById(obj.getAttrId());
                AttrVo attrVo=new AttrVo();
                BeanUtils.copyProperties(attrEntity,attrVo);
                attrVo.setAttrGroupId(obj.getAttrGroupId());
                return attrVo;
            }).collect(Collectors.toList());
            BeanUtils.copyProperties(item,withAttrRespVo);
            withAttrRespVo.setAttrs(attrVoList);
            return withAttrRespVo;
        }).collect(Collectors.toList());

        return withAttrRespVos;
    }

    @Override
    public List<SkuItemVo.SpuItemBaseAttrVo> selectSpuItemBaseAttrVo(Long catalogId, Long spuId) {
        List<SkuItemVo.SpuItemBaseAttrVo> groupAttrs=this.baseMapper.selectItemBaseAttr(catalogId,spuId);
        return groupAttrs;
    }


}