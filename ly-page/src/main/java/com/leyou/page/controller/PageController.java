package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/item/{id}.html")
    public String toGoodsDetailPage(Model model,@PathVariable("id") Long spuId){
        Map<String, Object> itemDataMap = pageService.loadItemPageData(spuId);
        model.addAllAttributes(itemDataMap);
        return "item";
    }


}