package com.leyou.item.controller;

import com.leyou.common.dto.PageResult;
import com.leyou.item.entity.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 保存品牌
     * @RequestParam 可以直接将逗号分割的字符串转成list集合。
     */
    @PostMapping("/brand")
    public ResponseEntity<Void> saveBrand(Brand brand,
                                          @RequestParam("cids") List<Long> cids){
        brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
