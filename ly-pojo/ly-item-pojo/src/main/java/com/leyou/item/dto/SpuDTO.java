package com.leyou.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.SpuDetail;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author 黑马程序员
 */
@Data
public class SpuDTO {
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String name;// 名称
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    private Date createTime;// 创建时间
    private String categoryName; // 商品分类名称拼接
    private String brandName;// 品牌名称

    private List<Sku> skus;
    private SpuDetail spuDetail;

    /**
     * 此方法是为了方便同时获取3级分类
     * 有了三级分类id的集合就很容易获取到三级分类名称的集合
     * @JsonIgnore注解是jackson中的注解，表示当前对象在转json的时候，忽略此属性【categoryIds】
     * 名称为getCategoryIds或者setCategoryIds的方法，都会被识别为set和get去掉的部分首字母小写的一个属性
     * 其余的名字都不会有这个问题，比如：方法名叫abc， eee，  fff的方法都仅仅是一个方法
     * @return
     */
    @JsonIgnore
    public List<Long> getCategoryIds(){
        return Arrays.asList(cid1, cid2, cid3);
    }
}