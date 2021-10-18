package com.xiaochen.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.product.entity.SkuImagesEntity;
import com.xiaochen.gulimall.product.entity.SpuInfoDescEntity;
import com.xiaochen.gulimall.product.entity.SpuInfoEntity;
import com.xiaochen.gulimall.product.feign.SeckillFeignService;
import com.xiaochen.gulimall.product.service.*;
import com.xiaochen.gulimall.product.vo.respone.SeckillInfoVo;
import com.xiaochen.gulimall.product.vo.respone.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.SkuInfoDao;
import com.xiaochen.gulimall.product.entity.SkuInfoEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }


    @Override
    public PageUtils skuInfoList(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String cateLogId=(String) params.get("catelogId");
        String brandId=(String)  params.get("brandId");
        String min=(String) params.get("min");
        String max=(String) params.get("max");
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
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

        if(!StringUtils.isEmpty(min)&&!min.equals("0")){
            wrapper.and(w->{
                w.ge("price",min);
            });
        }
        if(!StringUtils.isEmpty(max)&&!max.equals("0")){
            wrapper.and(w->{
                w.le("price",max);
            });
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusById(Long spuId) {
        return this.baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
    }

    @Override
    public SkuItemVo getSkuItemInfo(String skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        Long skuIdL = Long.parseLong(skuId);
        SkuInfoDao baseMapper = this.baseMapper;
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1 sku基本信息
            SkuInfoEntity info = getById(skuIdL);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2 sku图片信息
            List<SkuImagesEntity> images = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuIdL));
            skuItemVo.setImages(images);
        }, executor);


        CompletableFuture<Void> saleAttrFuture =infoFuture.thenAcceptAsync(res -> {
            //3 获取spu销售属性组合
            List<SkuItemVo.SkuItemSaleEntity> saleAttr=skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttr);
            System.out.println("chenxiao"+res.getSpuId()+" "+saleAttr);
        },executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //4 获取spu介绍
            System.out.println("chenxiao"+res.getSpuId()+" "+res);
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        },executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //5 获取spu规格参数信息
            Long catalogId = res.getCatalogId();
            Long spuId = res.getSpuId();
            List<SkuItemVo.SpuItemBaseAttrVo> groupAttrs=attrGroupService.selectSpuItemBaseAttrVo(catalogId,spuId);
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R skuSeckillInfo = seckillFeignService.getSkuSeckillInfo(skuIdL);
            if (skuSeckillInfo.getCode() == 0) {
                SeckillInfoVo data = skuSeckillInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                System.out.println("秒杀信息"+skuItemVo.getSeckillInfoVo());
                skuItemVo.setSeckillInfoVo(data);
            }
        });


        //等待所有线程执行完毕
        CompletableFuture.allOf(imageFuture,saleAttrFuture,descFuture,baseAttrFuture,seckillFuture).get();


        return skuItemVo;
    }

}