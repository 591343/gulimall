package com.xiaochen.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.mysql.cj.util.StringUtils;
import com.xiaochen.gulimall.product.entity.AttrGroupEntity;
import com.xiaochen.gulimall.product.service.CategoryBrandRelationService;
import com.xiaochen.gulimall.product.vo.respone.Catalog2Vo;
import com.xiaochen.gulimall.product.vo.respone.Catalog3Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.product.dao.CategoryDao;
import com.xiaochen.gulimall.product.entity.CategoryEntity;
import com.xiaochen.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Cacheable({"category"})
    @Override
    public List<CategoryEntity> listTree() {
        System.out.println("第一次调用");
        List<CategoryEntity> list=baseMapper.selectList(null);
        //选出顶级分类
        List<CategoryEntity> firstLevel=list.stream().filter(item-> item.getCatLevel().equals(1)).collect(Collectors.toList());
        /**
         * 按分类级别进行排序，sort越小级别越大
         */
        List<CategoryEntity> tree=firstLevel.stream()
        .map(item->{
            item.setChildren(findChildren(item,list));
            return item; })
        .sorted((o1, o2) -> (o1.getSort()==null?0:o1.getSort())-(o2.getSort()==null?0:o2.getSort()))
        .collect(Collectors.toList());

        return tree;
    }

    /**
     * 逻辑删除,分类引用的不能删除
     * @param catIds
     */
    @Override
    public void removeCategoryByIds(Long[] catIds) {
        //TODO:分类删除
        baseMapper.deleteBatchIds(Arrays.asList(catIds));
    }


    /**
     * 递归调用来查询子分类
     * @param father
     * @param all
     * @return
     */
    private List<CategoryEntity> findChildren(CategoryEntity father,List<CategoryEntity> all){
        /**
         * 按分类级别进行排序，sort越小级别越大
         */
        List<CategoryEntity> children=all.stream()
        .filter(item-> item.getParentCid().equals(father.getCatId())).map(item->{
            item.setChildren(findChildren(item,all));
            return item; })
        .sorted((o1, o2) -> (o1.getSort()==null?0:o1.getSort())-(o2.getSort()==null?0:o2.getSort()))
        .collect(Collectors.toList());

        return children;
    }

    @Override
    public Long [] findPath(Long cateLogId) {
        List<Long> list=new ArrayList<>();
        recurFindPath(cateLogId,list);
        Collections.reverse(list);
        return list.toArray(new Long[list.size()]);
    }

    /**
     *读多写少，采取失效模式（先写后删）
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("updatecascade-lock");
        RLock rLock = readWriteLock.writeLock();
        rLock.lock();
        try {
            this.updateById(category);
            if (!StringUtils.isEmptyOrWhitespaceOnly(category.getName())) {
                categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            }
            stringRedisTemplate.delete("catalogsJson");
        }catch (Exception e){

        }finally {
            rLock.unlock();
        }
        //TODO级联更新
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogs(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("updatecascade-lock");
        RLock rLock = readWriteLock.readLock();
        String str=null;
        try {
            str=stringRedisTemplate.opsForValue().get("catalogsJson");
            if(StringUtils.isEmptyOrWhitespaceOnly(str)){
                List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
                Map<String, List<Catalog2Vo>> map=this.redisCacheCatalogs(categoryEntities);
                //往redis存对象统一存json,为了跨平台
                String mapStr= JSON.toJSONString(map);
                stringRedisTemplate.opsForValue().set("catalogsJson",mapStr);
                return map;
            }
        }catch (Exception e){

        }finally {
            rLock.unlock();
        }

        return JSON.parseObject(str,new TypeReference<Map<String, List<Catalog2Vo>>>(){});
    }

    public Map<String, List<Catalog2Vo>> redisCacheCatalogs(List<CategoryEntity> categoryEntities) {

        Map<String, List<Catalog2Vo>> map = categoryEntities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> category2 = getParentCid(categoryEntities,v.getCatId());
            List<Catalog2Vo> collect2 = category2.stream().map(item -> {
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                catalog2Vo.setCatalog1Id(v.getCatId().toString());
                catalog2Vo.setId(item.getCatId().toString());
                catalog2Vo.setName(item.getName());
                List<CategoryEntity> category3 = getParentCid(categoryEntities,item.getCatId());
                List<Catalog3Vo> collect3 = category3.stream().map(item3 -> {
                    Catalog3Vo catalog3Vo = new Catalog3Vo();
                    catalog3Vo.setCatalog2Id(item.getCatId().toString());
                    catalog3Vo.setName(item3.getName());
                    catalog3Vo.setId(item3.getCatId().toString());
                    return catalog3Vo;
                }).collect(Collectors.toList());
                catalog2Vo.setCatalog3List(collect3);
                return catalog2Vo;
            }).collect(Collectors.toList());
            return collect2;
        }));
        return map;
    }

    public List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntities,Long parentCid){
        return categoryEntities.stream().filter(item->item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }


    private  void recurFindPath(Long cateLogId,List<Long> list){
        Long parentId=getById(cateLogId).getParentCid();
        list.add(cateLogId);
        if(parentId!=0) {
            recurFindPath(parentId, list);
        }
    }

}