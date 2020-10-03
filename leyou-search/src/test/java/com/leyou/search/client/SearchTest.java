package com.leyou.search.client;

import com.leyou.LeyouSearchApplication;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchApplication.class)
public class SearchTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void test() throws IOException {
        Spu spu = this.goodsClient.querySpuById(3l);
        Goods goods = this.searchService.buildGoods(spu);
        String skus = goods.getSkus();
        System.out.println(skus);
    }
}
