package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpecController {

    @Autowired
    private SpecService specService;

    /**
     * 根据分类id查询规格组列表
     */
    @GetMapping("/spec/groups/of/category")
    public ResponseEntity<List<SpecGroup>> findSpecGroupByCid(@RequestParam("id") Long id) {
        List<SpecGroup> list = specService.findSpecGroupByCid(id);
        //return ResponseEntity.status(HttpStatus.OK).body(list); 效果和下面的一样
        return ResponseEntity.ok(list);

    }

    /**
     * 查询规格参数列表
     * 注意：这里searching参数的类型必须是大写Boolean，默认值为null。而不是小写boolean，默认值为false
     */
    @GetMapping("/spec/params")
    public ResponseEntity<List<SpecParam>> findSpecParams(@RequestParam(value = "gid", required = false) Long gid,
                                                          @RequestParam(value = "cid", required = false) Long cid,
                                                          @RequestParam(value = "searching", required = false) Boolean searching){
        List<SpecParam> list = specService.findSpecParams(gid, cid, searching);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据分类id查询规格组dto列表，里面要保护规格参数信息
     */
    @GetMapping("/spec/groupsdto/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupDtoByCid(@RequestParam("id") Long id){
        List<SpecGroupDTO> list = specService.findSpecGroupDtoByCid(id);
        return ResponseEntity.ok(list);
    }
}