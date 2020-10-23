package com.leyou.search.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;
import java.util.Set;

/**
 * 一个SPU对应一个Goods
 * @Document中的属性说明
 * indexName索引库的名称goods
 * type表示文档docs
 */
@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 1)
public class Goods {
    @Id
    @Field(type = FieldType.Keyword)
    private Long id; // spuId
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;// 卖点
    // 在elasticSearch中，默认集合或map类型，都会对其所有属性进行索引，
    // 如果这个集合中的字段没必要被搜索匹配，就不要用集合来定义这个属性
    // 可以做成一个json格式的字符串来存储，真正使用的时候，我们可以取出数据，自己转成集合
    @Field(type = FieldType.Keyword, index = false)
    private String skus;// sku信息的json结构

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String spuName; // 当前Spu的名称 只要是字符串类型的属性都会索引和分词

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌

    //如果任何注解都不加，其实走的是默认配置，默认会根据当前类型自动识别是否分词，比如Long是会索引的但是不分词
    private Long brandId;// 品牌id
    private Long categoryId;// 商品3级分类id
    private Long createTime;// spu创建时间
    private Set<Long> price;// 价格
    //在elasticSearch中，默认集合或map类型，都会对其所有属性进行索引，
    //默认情况下集合或者map中的数据都会被识别成为keyword来对待，不分词。
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
}