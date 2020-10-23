package com.leyou.item.client;

import com.leyou.common.dto.PageResult;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/10/6 11:32
 */
@FeignClient("item-service")
public interface ItemClient {
    /**
     * 商品列表分页查询
     * 这里的商品一般是把一个spu看作一个商品
     * 只有在购买下单的时候，才把一个sku看作一个商品
     */
    @GetMapping("/spu/page")
    public PageResult<SpuDTO> goodsPageQuery(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                             @RequestParam(value = "key", required = false) String key,
                                             @RequestParam(value = "saleable", required = false) Boolean saleable);

    /**
     * 根据SpuId查询Sku集合
     */
    @GetMapping("/sku/of/spu")
    public List<Sku> findSkuBySpuId(@RequestParam("id") Long id);

    /**
     * 根据SpuId查询SpuDetail
     */
    @GetMapping("/spu/detail")
    public SpuDetail findSpuDetailBySpuId(@RequestParam("id") Long id);

    /**
     * 查询规格参数列表
     * 注意：这里searching参数的类型必须是大写Boolean，默认值为null。而不是小写boolean，默认值为false
     */
    @GetMapping("/spec/params")
    public List<SpecParam> findSpecParams(@RequestParam(value = "gid", required = false) Long gid,
                                          @RequestParam(value = "cid", required = false) Long cid,
                                          @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 根据分类id集合查询分类对象集合
     */
    @GetMapping("/category/list")
    public List<Category> queryCategorysByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据品牌id查询品牌的对象
     */
    @GetMapping("/brand/{id}")
    public Brand findBrandById(@PathVariable("id") Long id);

    /**
     * 根据spuId查询SpuDTO对象
     */
    @GetMapping("/spu/{id}")
    public SpuDTO findSpuDtoById(@PathVariable("id") Long id);

    @GetMapping("/spec/groupsdto/of/category")
    public List<SpecGroupDTO> findSpecGroupDtoByCid(@RequestParam("id") Long id);
}

