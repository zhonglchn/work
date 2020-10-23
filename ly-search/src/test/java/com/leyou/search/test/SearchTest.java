package com.leyou.search.test;

import com.leyou.common.dto.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.SpecParam;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.SearchRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/10/6 11:34
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchRepository searchRepository;
//
//    /**
//     * 将数据源的数据导入索引库
//     */
//    @Test
//    public void buildIndex(){
//        int page = 1, pages=1;
//        do{
//            //分页查询SpuDTO对象
//            PageResult<SpuDTO> pageResult = itemClient.goodsPageQuery(page, 100, null, true);
//            //获取到当前SpuDTO集合
//            List<SpuDTO> spuDTOS = pageResult.getItems();
//            //遍历spuDTOS
//            spuDTOS.forEach(spuDTO -> {
//                //将SpuDTO转成Goods对象
//                Goods goods = searchService.buildGoods(spuDTO);
//                //保存索引库
//                searchRepository.save(goods);
//            });
//            //给总页数赋值
//            pages = pageResult.getTotalPage();
//            //页码加一
//            page++;
//        }while (page<=pages);
//    }

    /**
     * 测试feign
     */
    @Test
    public void findSpecParams(){
        List<SpecParam> specParams = itemClient.findSpecParams(null, 76l, true);
        System.out.println(specParams);
    }

}
