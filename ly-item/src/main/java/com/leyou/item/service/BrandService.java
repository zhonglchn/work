package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.PageResult;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/9/24 14:58
 */
@Service
@Transactional
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> brandPageQuery(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Brand.class);

        //判断key是否有值
        if(StringUtils.isNotBlank(key)){
            Example.Criteria criteria = example.createCriteria();
            criteria.orLike("name","%"+key+"%");
            criteria.orEqualTo("letter",key.toUpperCase());
        }
        //判断是否有排序字段  select * from xxx order by xxx desc
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc?"desc":"asc"));
        }

        //数据库查询【分页查询】
        List<Brand> brands = brandMapper.selectByExample(example);

        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), brands);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), brands);
    }
}
