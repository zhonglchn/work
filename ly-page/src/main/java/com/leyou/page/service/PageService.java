package com.leyou.page.service;

import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhongliang
 * @date 2020/10/19 15:10
 */
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    //模板引擎
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${ly.static.itemDir}")
    private String itemDir;//静态页面服务器地址

    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;

    public Map<String, Object> loadItemPageData(Long spuId) {
        //根据id查询SpuDTO对象  暂时没有需要自己写
        SpuDTO spuDTO = itemClient.findSpuDtoById(spuId);
        //根据SpuDTO中三级分类的集合查询三级分类对象  有了
        List<Category> categories = itemClient.queryCategorysByIds(spuDTO.getCategoryIds());
        //根据SpuDTO中品牌的id查询出品牌的对象  有了
        Brand brand = itemClient.findBrandById(spuDTO.getBrandId());
        //根据第三级分类查询出规格组  暂时没有需要自己写
        List<SpecGroupDTO> groupDTOS = itemClient.findSpecGroupDtoByCid(spuDTO.getCid3());

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("spuName", spuDTO.getName());//spu的名称    根据id查询SpuDTO对象
        itemData.put("subTitle", spuDTO.getSubTitle());//商品的副标题    根据id查询SpuDTO对象
        itemData.put("detail", spuDTO.getSpuDetail());//商品详情    根据id查询SpuDTO对象
        itemData.put("skus", spuDTO.getSkus());//当前spu下所有sku的集合    根据id查询SpuDTO对象
        itemData.put("categories", categories);//商品三级分类对象的集合   根据SpuDTO中三级分类的集合查询三级分类对象
        itemData.put("brand", brand);//品牌对象   根据SpuDTO中品牌的id查询出品牌的对象
        itemData.put("specs", groupDTOS);//规格组集合 每个规格组对象内要保护当前规格组下的所有规格参数集合  根据第三级分类查询出规格组
        return itemData;
    }

    /**
     * 编写生成静态页面的service
     */
    public void createItemStaticPage(Long spuId){
        //准备上下文
        Map<String, Object> map = loadItemPageData(spuId);
        Context context = new Context();
        context.setVariables(map);
        //创建一个静态页面的文件对象
        File pageFile = new File(new File(itemDir), spuId + ".html");
        //创建一个打印流
        try( PrintWriter printWriter = new PrintWriter(pageFile)) {
           // 生成静态页面
            templateEngine.process(itemTemplate,context,printWriter);
        } catch (FileNotFoundException e) {
          throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }
    }

    public void delStaticPage(Long id) {
        //得到当前要删除的页面文件
        File pageFile = new File(new File(itemDir), id+".html");
        if(pageFile.exists()){
            pageFile.delete();
        }
    }
}

