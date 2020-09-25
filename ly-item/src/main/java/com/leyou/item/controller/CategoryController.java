package com.leyou.item.controller;

import com.leyou.item.entity.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/9/23 15:03
 */
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category/of/parent")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid){
      List<Category> list =  categoryService.queryCategoryByPid(pid);
      return ResponseEntity.ok(list);
    }

}
