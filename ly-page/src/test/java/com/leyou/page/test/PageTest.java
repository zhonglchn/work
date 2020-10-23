package com.leyou.page.test;

import com.leyou.page.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhongliang
 * @date 2020/10/19 16:06
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PageTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createItemPage(){
        pageService.createItemStaticPage(81L);
    }
}
