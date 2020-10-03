package com.leyou.item.test;

import com.leyou.LeyouItemServiceApplication;
import com.leyou.item.pojo.Sku;
import com.leyou.service.impl.GoodsServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouItemServiceApplication.class)
public class ItemTest {

    @Autowired
    private GoodsServiceImpl goodsService;

    @Test
    public void test(){
        List<Sku> skus = this.goodsService.querySkusBySpuId(3l);
        for (Sku sku : skus) {
            System.out.println(sku);
        }
    }
}
