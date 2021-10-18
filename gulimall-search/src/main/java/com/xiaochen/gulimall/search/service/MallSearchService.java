package com.xiaochen.gulimall.search.service;

import com.xiaochen.gulimall.search.vo.request.SearchParam;
import com.xiaochen.gulimall.search.vo.response.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
