package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/9/24 14:58
 */
public interface BrandMapper extends Mapper<Brand> {

    void insertCategoryAndBrand(@Param("cids") List<Long> cids, @Param("bid")Long id);
}
