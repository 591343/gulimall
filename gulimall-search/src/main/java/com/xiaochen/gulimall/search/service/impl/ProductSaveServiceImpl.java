package com.xiaochen.gulimall.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.xiaochen.common.to.es.SkuEsModel;
import com.xiaochen.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.xiaochen.gulimall.search.constant.EsConstant;
import com.xiaochen.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Override
    public Boolean productStatsUp(List<SkuEsModel> skuEsModels) throws IOException {
        //执行批量sku保存
        BulkRequest bulkRequest=new BulkRequest();
        for (SkuEsModel skuEsModel:skuEsModels) {
            String jsonStr= JSON.toJSONString(skuEsModel);
            bulkRequest.add(new IndexRequest(EsConstant.PRODUCT_STATS_UP_INDEX)
                    .id(String.valueOf(skuEsModel.getSkuId()))
                    .source(jsonStr, XContentType.JSON));
        }
        BulkResponse response =restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        Boolean hasFailure=response.hasFailures();
        BulkItemResponse[] items = response.getItems();
        List<Integer> itemIds=Arrays.stream(items).filter(BulkItemResponse::isFailed).map(BulkItemResponse::getItemId).collect(Collectors.toList());
        log.error("商品上架批量保存失败ID:{}",itemIds);

        return hasFailure;
    }
}
