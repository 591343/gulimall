package com.xiaochen.gulimall.product.web;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaochen.gulimall.product.entity.CategoryEntity;
import com.xiaochen.gulimall.product.service.CategoryService;
import com.xiaochen.gulimall.product.vo.respone.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> list = categoryService.list(new QueryWrapper<CategoryEntity>().eq("cat_level",1));
        model.addAttribute("categories",list);
        return "index";
    }

    /**
     * 以特定形式获取分类目录
     * @return
     */
    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogs(){

        return categoryService.getCatalogs();
    }
}
