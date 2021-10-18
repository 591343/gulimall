package com.xiaochen.gulimall.order.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/{page}.html")
    public String getPage(@PathVariable("page") String page){
        System.out.println(page);
        return page;
    }
}
