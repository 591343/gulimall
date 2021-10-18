package com.xiaochen.gulimall.search.service;

import com.xiaochen.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface ProductSaveService {
    Boolean productStatsUp(List<SkuEsModel> skuEsModels) throws IOException;
}
