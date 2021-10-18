package com.xiaochen.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xiaochen.common.constant.ProductConstant;
import com.xiaochen.common.to.HasStockTo;
import com.xiaochen.common.to.SkuReductionTo;
import com.xiaochen.common.to.SpuBoundTo;
import com.xiaochen.common.to.es.SkuEsModel;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.product.entity.*;
import com.xiaochen.gulimall.product.feign.CouponFeignService;
import com.xiaochen.gulimall.product.feign.SearchFeignService;
import com.xiaochen.gulimall.product.feign.WareFeignService;
import com.xiaochen.gulimall.product.service.*;
import com.xiaochen.gulimall.product.vo.request.*;
import com.xiaochen.gulimall.product.vo.respone.AttrRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date(new java.util.Date().getTime()));
        infoEntity.setUpdateTime(new Date(new java.util.Date().getTime()));
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc

        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);



        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(),images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if((Integer) r.get("code") != 0){
            log.error("远程保存spu积分信息失败");
        }


        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size()>0){
            skus.forEach(item->{
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if((Integer) r1.get("code") != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {

        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        return null;
    }

    @Override
    public PageUtils spuInfoList(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String cateLogId=(String) params.get("catelogId");
        String brandId=(String)  params.get("brandId");
        String status=(String) params.get("status");
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key);
            });

        }
        if(!StringUtils.isEmpty(cateLogId)&&!cateLogId.equals("0")){
            wrapper.and(w->{
                w.eq("catalog_id",cateLogId);
            });

        }
        if(!StringUtils.isEmpty(brandId)&&!brandId.equals("0")){
            wrapper.and(w->{
                w.eq("brand_id",brandId);
            });
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.and(w->{
                w.eq("publish_status",status);
            });
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper

        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void skuUp(Long spuId) {

        //1.组装SkuEsModel
        List<SkuInfoEntity> skuInfoEntities=skuInfoService.getSkusById(spuId);
        List<ProductAttrValueEntity> productAttrValueList=productAttrValueService.getProductAttrValueList(spuId);
        List<SkuEsModel.Attr> attrs=productAttrValueList.stream().filter(obj->{
            AttrRespVo attrInfo = attrService.getAttrInfo(obj.getAttrId());
            if(attrInfo.getSearchType().equals(ProductConstant.AttrSearchTypeEnum.ATTR_ENABLE_SEARCH.getCode())){
                return true;
            }
            return false;
        }).map(item->{
            SkuEsModel.Attr attr=new SkuEsModel.Attr();
            BeanUtils.copyProperties(item,attr);
            return  attr;
        }).collect(Collectors.toList());

        List<Long> skuIds=skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //查询库存
        Map<Long,Boolean> collect=new HashMap<>();
        try {
            R r = wareFeignService.hasStock(skuIds);
            TypeReference<List<HasStockTo>> typeReference=new TypeReference<List<HasStockTo>>(){};
            collect=r.getData(typeReference).stream().collect(Collectors.toMap(HasStockTo::getSkuId, HasStockTo::getHasStock));
        }catch (Exception e){
            log.error("是否有库存服务RPC调用异常",e);
        }

        Map<Long, Boolean> finalCollect = collect;

        List<SkuEsModel> skuEsModels=skuInfoEntities.stream().map(item->{
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(item,skuEsModel);
            skuEsModel.setSkuPrice(item.getPrice());
            skuEsModel.setSkuImg(item.getSkuDefaultImg());
            skuEsModel.setHotScore(0L);
            skuEsModel.setHasStock(finalCollect.getOrDefault(item.getSkuId(),true));
            BrandEntity brandEntity=brandService.getById(item.getBrandId());
            if(brandEntity!=null){
                skuEsModel.setBrandName(brandEntity.getName());
                skuEsModel.setBrandImg(brandEntity.getLogo());
                CategoryBrandRelationEntity categoryBrandRelationEntity=categoryBrandRelationService.getById(brandEntity.getBrandId());
                if(categoryBrandRelationEntity!=null){
                    skuEsModel.setCatalogId(categoryBrandRelationEntity.getCatelogId());
                    skuEsModel.setCatalogName(categoryBrandRelationEntity.getCatelogName());
                }
            }

            //设置检索属性
            skuEsModel.setAttrs(attrs);
            //RPCC查询ware服务是否有库存
            return skuEsModel;
        }).collect(Collectors.toList());

        //发送给ES进行存储，RPC调用search服务
        R r = searchFeignService.saveSkuProduct(skuEsModels);
        if((Integer) r.get("code")==0){
            //修改spu状态
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setPublishStatus(ProductConstant.StatsEnum.SPU_UP.getCode());
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setUpdateTime(new Date(new java.util.Date().getTime()));
            baseMapper.updateById(spuInfoEntity);
        }else {
            //远程调用服务失败
            //TODO 可能存在重复调用
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        return getById(skuInfoService.getById(skuId).getSpuId());
    }

}