package com.leyou.item.controller;

import com.leyou.common.dto.PageResult;
import com.leyou.item.entity.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhongliang
 * @date 2020/9/24 15:13
 */
@RestController
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("/brand/page")
    public ResponseEntity<PageResult<Brand>> brandPageQuery(@RequestParam(value = "key",required = false) String key,
                                                            @RequestParam(value = "page",defaultValue = "1") Integer page,
                                                            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
                                                            @RequestParam(value = "sortBy",required = false) String sortBy,
                                                            @RequestParam(value = "desc",required = false) Boolean desc){

        PageResult<Brand> pageResult = brandService.brandPageQuery(key,page,rows,sortBy,desc);
        return ResponseEntity.ok(pageResult);

    }
}
