package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.constant.MQConstants;
import com.leyou.common.dto.PageResult;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.*;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuDTO> goodsPageQuery(Integer page, Integer rows, String key, Boolean saleable) {
        PageHelper.startPage(page, rows);

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //判断是否有查询条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%");
        }
        //判断是否有上下架条件
        if (saleable != null) {
            //如果saleable为true，数据库中对应的tinyint类型字段会自动赋值为1，反之为0.
            criteria.andEqualTo("saleable", saleable);
        }
        //按照添加时间倒叙，注意这里是原生的sql语句，要写数据库中的字段
        example.setOrderByClause("create_time desc");
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //封装一个pagehelp的分页对象PageInfo
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        //先将当前页的Spu数据转成SpuDTO数据
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(pageInfo.getList(), SpuDTO.class);
        //要给SpuDTO集合中每个对象的分类名称【一级分类|二级分类|三级分类】和品牌名称【华为】赋值
        handleCategoryNamesAndBrandName(spuDTOS);

        //通过PageInfo封装一个自定义的分页对象PageResult
        PageResult<SpuDTO> pageResult = new PageResult<>(pageInfo.getTotal(),
                pageInfo.getPages(),
                spuDTOS);
        return pageResult;
    }

    private void handleCategoryNamesAndBrandName(List<SpuDTO> spuDTOS) {
        //遍历SpuDTO集合
        spuDTOS.forEach(spuDTO -> {
            //获取到分类的id的集合
            List<Long> categoryIds = spuDTO.getCategoryIds();
            //通过分类id的集合查询分类对象的集合
            String categoryNames = categoryService.queryCategorysByIds(categoryIds)//得到分类对象的集合
                    .stream()//将分类对象的集合转成流
                    .map(Category::getName)//得到每个Category对象中的name值，现在这些名称都是零散的状态
                    .collect(Collectors.joining("|"));//将这些分类的名称以|拼接成一个字符串
            //把分类名称赋值给SpuDTO对象
            spuDTO.setCategoryName(categoryNames);

            //得到品牌的名称
            String brandName = brandService.findBrandById(spuDTO.getBrandId()).getName();
            //把品牌名称赋值给SpuDTO对象
            spuDTO.setBrandName(brandName);
        });
    }

    public void saveGoods(SpuDTO spuDTO) {
        try {
            //先将SpuDTO对象转成Spu对象
            Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
            //设置商品状态为下架
            spu.setSaleable(false);
            //保存Spu对象，这里必须选择insertSelective方法，才能在自动生成sql时不会有日期字段，那么日期就会走数据库的默认设置
            spuMapper.insertSelective(spu);

            //获取新增的Spu对象的id
            Long spuId = spu.getId();

            //得到SpuDetail对象
            SpuDetail spuDetail = spuDTO.getSpuDetail();
            //给spuId属性赋值
            spuDetail.setSpuId(spuId);
            //保存SpuDetail
            detailMapper.insertSelective(spuDetail);

            //获取Sku集合
            List<Sku> skus = spuDTO.getSkus();
            //遍历Sku集合，给每个Sku对象中的SpuId赋值
            skus.forEach(sku -> {
                sku.setSpuId(spuId);
                //给当前日期和更新日期赋值
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
            });
            //保存Sku集合
            skuMapper.insertList(skus);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 商品上下架
     *
     * @param id
     * @param saleable
     */
    public void updateSaleable(Long id, Boolean saleable) {
        try {
            Spu record = new Spu();
            record.setId(id);
            record.setSaleable(saleable);
            spuMapper.updateByPrimaryKeySelective(record);
            //这里如果上架商品，应该生成一个静态也并向索引库中添加一条记录
            //如果下架商品，应该删除一个静态页并删除一条索引库数据
            //参数一：交换机exchange, 参数二 routingKey, 参数三：发送的内容
            amqpTemplate.convertAndSend(MQConstants.Exchange.ITEM_EXCHANGE_NAME,
                    saleable ? MQConstants.RoutingKey.ITEM_UP_KEY : MQConstants.RoutingKey.ITEM_DOWN_KEY,
                    id);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public SpuDetail findSpuDetailBySpuId(Long id) {
        SpuDetail spuDetail = detailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> findSkuBySpuId(Long id) {
        Sku record = new Sku();
        record.setSpuId(id);
        List<Sku> skus = skuMapper.select(record);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return skus;
    }

    public void updateGoods(SpuDTO spuDTO) {
        try {
            //得到Spu
            Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
            //修改Spu
            spuMapper.updateByPrimaryKeySelective(spu);

            //得到SpuDetail
            SpuDetail spuDetail = spuDTO.getSpuDetail();
            //修改SpuDetail
            detailMapper.updateByPrimaryKeySelective(spuDetail);

            //获取当前的Spu对象的id
            Long spuId = spu.getId();

            //删除当前Spu下所有Sku列表
            Sku record = new Sku();
            record.setSpuId(spuId);
            skuMapper.delete(record);

            //获取Sku集合
            List<Sku> skus = spuDTO.getSkus();
            //遍历Sku集合，给每个Sku对象中的SpuId赋值
            skus.forEach(sku -> {
                sku.setSpuId(spuId);
                //给当前日期和更新日期赋值
                sku.setCreateTime(new Date());
                sku.setUpdateTime(new Date());
            });
            //保存Sku集合
            skuMapper.insertList(skus);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public SpuDTO findSpuDtoById(Long id) {
        //根据id查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //将Spu转成DTO
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //将sku的集合封装到SpuDTO中
        spuDTO.setSkus(findSkuBySpuId(id));
        //将SpuDetail的对象封装到SpuDTO中
        spuDTO.setSpuDetail(findSpuDetailBySpuId(id));
        return spuDTO;
    }
}