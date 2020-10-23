package com.leyou.search.controller;

import com.leyou.common.dto.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 查询商品列表
     */
    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> goodsPageQuery(@RequestBody SearchRequest request){
        PageResult<GoodsDTO> result = searchService.goodsPageQuery(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 加载过滤条件
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> filterParamQuery(@RequestBody SearchRequest request){
        Map<String, List<?>> map = searchService.filterParamQuery(request);
        return ResponseEntity.ok(map);
    }

}