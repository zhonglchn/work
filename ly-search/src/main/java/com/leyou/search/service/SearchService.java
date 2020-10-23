package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.dto.PageResult;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.SearchRepository;
import com.leyou.search.utils.HighlightUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SearchRepository searchRepository;

    /**
     * 提供一个将SpuDTO对象转成Goods对象的业务方法
     */
    public Goods buildGoods(SpuDTO spuDTO){
        //根据spuId查询Sku集合
        List<Sku> skus = itemClient.findSkuBySpuId(spuDTO.getId());
        //定义一个包装类dto或者map来讲sku集合中所用到的字段放进来
        List<Map<String, Object>> skuList = new ArrayList<>();
        //遍历skus
        skus.forEach(sku -> {
            Long id = sku.getId();
            String title = sku.getTitle().substring(spuDTO.getName().length());//这里只要除去spuName之外的部分
            Long price = sku.getPrice();
            String image = StringUtils.substringBefore(sku.getImages(), ",");
            //把上面四个属性放入到一个新的Map中
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", id);
            skuMap.put("title", title);
            skuMap.put("price", price);
            skuMap.put("image", image);
            //将skuMap放入到skuList中
            skuList.add(skuMap);
        });

        //采用流的方式将sku集合中所有的价格收集成set集合
        Set<Long> prices = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());

        //创建一个动态过滤条件存放的map对象
        Map<String, Object> specs = new HashMap<>();
        //得到动态过滤条件所有的key值所在的规格参数对象集合
        List<SpecParam> specParams = itemClient.findSpecParams(null, spuDTO.getCid3(), true);
        //得到动态过滤条件所有的value值的来源对象
        SpuDetail spuDetail = itemClient.findSpuDetailBySpuId(spuDTO.getId());
        //得到通用规格参数所在的字段[相当于一张表或者一个javaBean对象]
        String genericSpecStr = spuDetail.getGenericSpec();
        //得到特有规格参数所在的字段[相当于一张表或者一个javaBean对象]
        String specialSpecStr = spuDetail.getSpecialSpec();
        //将通用规格参数所在的字段的值转成map
        Map<Long, Object> genericSpecJson = JsonUtils.toMap(genericSpecStr, Long.class, Object.class);
        //将特有规格参数所在的字段的值转成map
        Map<Long, List<Object>> specialSpecJson = JsonUtils.nativeRead(specialSpecStr, new TypeReference<Map<Long, List<Object>>>() {});
        //遍历key所在的规格参数对象集合specParams
        specParams.forEach(specParam -> {
            //获取key值
            String key = specParam.getName();
            //获取value值
            Object value = null;
            //根据是否是通用规格参数来判断当前规格参数值的来源
            if(specParam.getGeneric()){
                //通用的就从genericSpecJson取值
                value = genericSpecJson.get(specParam.getId());
            }else {
                //特有的就从specialSpecJson取值
                value = specialSpecJson.get(specParam.getId());
            }
            //如果当前的value值是一个数字，我们要兑换成区间存入索引库
            if(specParam.getNumeric()){
                //这里我们直接使用资料中提供好的算法，不要自己写，写了企业中也没人用java的算法
                value = chooseSegment(value, specParam);
            }
            //把key和value赋值给specs
            specs.put(key, value);
        });

        //创建一个Goods对象
        Goods goods = new Goods();
        goods.setId(spuDTO.getId());
        goods.setSpuName(spuDTO.getName());//用来搜索的字段
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setAll(spuDTO.getCategoryName()+spuDTO.getBrandName());//用来搜索的字段
        goods.setSkus(JsonUtils.toString(skuList));
        goods.setPrice(prices);
        goods.setSpecs(specs);
        return goods;
    }

    //算法的作用就是将数字类型的规格参数值，兑换成区间
    private String chooseSegment(Object value, SpecParam p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public PageResult<GoodsDTO> goodsPageQuery(SearchRequest request) {
        //封装复杂条件的对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //指定要查询的字段，或者要排除的字段
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "spuName", "subTitle", "skus"}, null));
        //封装分页参数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(request.getPage()-1, request.getSize()));
        //封装搜索条件或者 过滤条件
        nativeSearchQueryBuilder.withQuery(createQueryBuilder(request));
        // 指定高亮的字段
        HighlightUtils.highlightField(nativeSearchQueryBuilder,"spuName");
        //复杂条件查询
        AggregatedPage<Goods> pageGoods = esTemplate.queryForPage(nativeSearchQueryBuilder.build(),
                Goods.class,
                HighlightUtils.highlightBody(Goods.class,"spuName"));
        //创建一个自定义的分页对象
        PageResult<GoodsDTO> pageResult = new PageResult<>(
                pageGoods.getTotalElements(),//总记录数
                pageGoods.getTotalPages(),//总页数
                BeanHelper.copyWithCollection(pageGoods.getContent(), GoodsDTO.class)//数据
        );
        return pageResult;
    }

    /**
     * 构建查询条件 添加的条件保护搜索添加和过滤条件
     * @param request
     * @return
     */
    private QueryBuilder createQueryBuilder(SearchRequest request) {
        //Operator.AND表示让当前搜索内容分词后的词条，匹配的时候是and的关系，都要包含这些关键字才行
//        return QueryBuilders.multiMatchQuery(request.getKey(), "all", "spuName").operator(Operator.AND);

        //提供一个bool查询对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //添加搜索添加  Operator.AND表示让当前搜索内容分词后的词条，匹配的时候是and的关系，都要包含这些关键字才行
        boolQuery.must(QueryBuilders.multiMatchQuery(request.getKey(), "all", "spuName").operator(Operator.AND));
        //得到所有的过滤条件的map
        Map<String, Object> paramKey = request.getParamKey();
        //遍历map
        Set<Map.Entry<String, Object>> entries = paramKey.entrySet();
        entries.forEach(entry->{
            //得到map的key
            String key = entry.getKey();
            //得到map的value
            Object value = entry.getValue();
            //对key进行处理，使得key与索引库中的域名一致
            if(StringUtils.equals(key, "分类")){
                key = "categoryId";
            }else if (StringUtils.equals(key, "品牌")){
                key = "brandId";
            }else {
                key = "specs."+key+".keyword";
            }
            //添加过滤条件
            boolQuery.filter(QueryBuilders.termQuery(key, value));
        });
        return boolQuery;
    }

    /**
     * 过滤条件查询
     * @param request
     * @return
     */
    public Map<String, List<?>> filterParamQuery(SearchRequest request) {
        //创建一个过滤条件结果的map
        Map<String, List<?>> filterMap = new LinkedHashMap<>();
        //封装复杂条件的对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //指定要查询的字段，或者要排除的字段
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        //封装分页参数 这里表示固定查询第一页第一条记录
        nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 1));
        //封装搜索条件
        nativeSearchQueryBuilder.withQuery(createQueryBuilder(request));
        //给分类聚合起名称
        String categoryName = "categoryAgg";
        //添加分类的聚合条件，如果不写size默认查询十个，写的话就这么写：field("categoryId").size(Integer.MAX_VALUE)
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryName).field("categoryId"));
        //给品牌聚合起名称
        String brandName = "brandAgg";
        //添加品牌的聚合条件，如果不写size默认查询十个
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandName).field("brandId"));
        //复杂条件查询
        AggregatedPage<Goods> pageGoods = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        //得到所有的聚合结果
        Aggregations aggregations = pageGoods.getAggregations();
        //得到分类的聚合结果
        Terms categoryTerm = aggregations.get(categoryName);
        //将分类的聚合结果放到结果map中去
        List<Long> categoryIds = handlerCategoryAgg(filterMap, categoryTerm);
        //得到品牌的聚合结果
        Terms brandTerm = aggregations.get(brandName);
        //将品牌的聚合结果放到结果map中去
        handlerBrandAgg(filterMap, brandTerm);
        //封装规格参数过滤条件，封装规格参数过滤条件所需要的条件比封装分类和品牌多了一个分类的id集合
        handlerSpecParamFilter(filterMap, createQueryBuilder(request), categoryIds);
        return filterMap;
    }

    private void handlerSpecParamFilter(Map<String, List<?>> filterMap, QueryBuilder queryBuilder, List<Long> categoryIds) {
        categoryIds.forEach(cid->{
            //根据分类id查询出规格参数，注意查询出几个规格参数，就要添加几个聚合条件
            List<SpecParam> specParams = itemClient.findSpecParams(null, cid, true);

            //封装复杂条件的对象
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

            //指定要查询的字段，或者要排除的字段
            nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));

            //封装分页参数 这里表示固定查询第一页第一条记录
            nativeSearchQueryBuilder.withPageable(PageRequest.of(0,1));
            //封装搜索条件
            nativeSearchQueryBuilder.withQuery(queryBuilder);

            //遍历规格参数结果
            specParams.forEach(specParam -> {
                //给规格参数聚合起名称
                String specName = specParam.getName();
                //获取当前规格参数在索引库中的域名
                String field = "specs."+specName+".keyword";
                //添加规格参数的聚合条件，如果不写size默认查询十个，注意这里的field必须和索引库保持一直
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(specName).field(field));
            });

            //复杂条件查询
            AggregatedPage<Goods> pageGoods = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
            //得到所有的聚合结果
            Aggregations aggregations = pageGoods.getAggregations();

            //遍历规格参数结果
            specParams.forEach(specParam -> {
                //给规格参数聚合起名称
                String specName = specParam.getName();
                //得到规格参数的聚合结果
                Terms categoryTerm = aggregations.get(specName);
                //得到规格参数过滤条件的集合
                List<String> specParamList = categoryTerm.getBuckets()
                        .stream()
                        .map(Terms.Bucket::getKeyAsString)
                        .collect(Collectors.toList());
                //将规格参数过滤条件的集合放入到map中去
                filterMap.put(specName, specParamList);
            });
        });
    }

    /**
     * 处理品牌聚合结果
     * @param filterMap  存放聚合结果的map
     * @param brandTerm  聚合结果所在的容器
     */
    private void handlerBrandAgg(Map<String, List<?>> filterMap, Terms brandTerm) {
        List<Brand> brands = brandTerm.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .map(itemClient::findBrandById)
                .collect(Collectors.toList());
        //把结果放入到map中
        filterMap.put("品牌", brands);
    }
    /**
     * 处理分类聚合结果
     * @param filterMap  存放聚合结果的map
     * @param categoryTerm  聚合结果所在的容器
     */
    private List<Long> handlerCategoryAgg(Map<String, List<?>> filterMap, Terms categoryTerm) {
        List<Long> categoryIds = categoryTerm.getBuckets()//获取桶的集合
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)//得到每个桶中的key并转成Number
                .map(Number::longValue)//把Number转成Long
                .collect(Collectors.toList());//收集为List集合
        //根据分类的id集合查询分类对象的集合
        List<Category> categories = itemClient.queryCategorysByIds(categoryIds);
        //把结果放入到map中
        filterMap.put("分类", categories);
        return categoryIds;
    }

    public void addIndex(Long spuId) {
        try {
            SpuDTO spuDTO = itemClient.findSpuDtoById(spuId);
            Goods goods = buildGoods(spuDTO);
            searchRepository.save(goods);
        }catch (Exception e){
            //如果当前消息没有消费成功，只要消费者放抛出异常，就会回执消息
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    public void delIndex(Long spuId) {
        try {
            searchRepository.deleteById(spuId);
        }catch (Exception e){
            //如果当前消息没有消费成功，只要消费者放抛出异常，就会回执消息
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }
}