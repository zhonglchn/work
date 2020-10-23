package com.leyou.search.repository;

import com.leyou.search.entity.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

/**
 * @author zhongliang
 * @date 2020/10/6 11:43
 */
public interface SearchRepository extends ElasticsearchCrudRepository<Goods,Long> {
}
