package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/9/24 14:58
 */
public interface BrandMapper extends Mapper<Brand> {

    void insertCategoryAndBrand(@Param("cids") List<Long> cids, @Param("bid")Long id);

    @Select("SELECT b.* FROM tb_brand b, tb_category_brand cb " +
            "WHERE b.id=cb.brand_id AND cb.category_id = #{cid}")
    List<Brand> findBrandsByCategoryId(Long id);
}
