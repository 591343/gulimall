package com.xiaochen.gulimall.search.controller;


import com.xiaochen.common.to.es.SkuEsModel;
import com.xiaochen.gulimall.search.service.MallSearchService;
import com.xiaochen.gulimall.search.vo.request.SearchParam;
import com.xiaochen.gulimall.search.vo.response.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    /**
     * Searching products
     * @param searchParam
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String index(SearchParam searchParam, Model model){
        //检索商品
        SearchResult result =mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
