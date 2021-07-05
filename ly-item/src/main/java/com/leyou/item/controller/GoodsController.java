package com.leyou.item.controller;

import com.leyou.common.dto.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    /**
     * 商品列表分页查询
     * 这里的商品一般是把一个spu看作一个商品
     * 只有在购买下单的时候，才把一个sku看作一个商品
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> goodsPageQuery(@RequestParam(value = "page",defaultValue = "1") Integer page,
                                                             @RequestParam(value = "rows",defaultValue = "5") Integer rows,
                                                             @RequestParam(value = "key",required = false) String key,
                                                             @RequestParam(value = "saleable",required = false) Boolean saleable ){

        PageResult<SpuDTO> pageResult = goodsService.goodsPageQuery(page, rows, key, saleable);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 添加商品
     * 使用@RequestBody直接将json格式数据，此时参数的名称可以随便写
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
        goodsService.saveGoods(spuDTO);
        //如果是添加操作状态码用HttpStatus.CREATED，没有返回值就不需要写body直接build()就可以了
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 商品上下架
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(@RequestParam("id") Long id,
                                               @RequestParam("saleable") Boolean saleable){
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据SpuId查询SpuDetail
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetail> findSpuDetailBySpuId(@RequestParam("id") Long id){
        SpuDetail spuDetail = goodsService.findSpuDetailBySpuId(id);
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据SpuId查询Sku集合
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<Sku>> findSkuBySpuId(@RequestParam("id") Long id){
        List<Sku> skus = goodsService.findSkuBySpuId(id);
        return ResponseEntity.ok(skus);
    }

    /**
     * 修改商品
     * 使用@RequestBody直接将json格式数据，此时参数的名称可以随便写
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        //如果是修改操作状态码用HttpStatus.NO_CONTENT，没有返回值就不需要写body直接build()就可以了
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询SpuDTO对象
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuDtoById(@PathVariable("id") Long id){
        SpuDTO spuDTO = goodsService.findSpuDtoById(id);
        return ResponseEntity.ok(spuDTO);
    }

    /**
     * 根据sku的id集合查询sku对象集合
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> findSkusByIds(@RequestParam("ids") List<Long> ids){
        List<Sku> list = goodsService.findSkusByIds(ids);
        return ResponseEntity.ok(list);
    }

    /**
     * 减库存
     */
    @PutMapping("/stock/minus")
    public ResponseEntity<Void> minusStock(@RequestBody Map<Long, Integer> paramMap){
        goodsService.minusStock(paramMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}