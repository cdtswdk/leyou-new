package com.leyou.search.client;

import com.leyou.LeyouSearchApplication;
import com.leyou.item.pojo.Brand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchApplication.class)
public class BrandClientTest {
    @Autowired
    private BrandClient brandClient;

    @Test
    public void QueryBrand(){
        Brand brand = this.brandClient.queryBrandById(1528L);
        System.out.println(brand);
    }
}