package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> map = new HashMap<>();

        //查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);

        //查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);

        //查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        //查询分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> categoryMap = new HashMap<>();
            categoryMap.put("id",cids.get(i));
            categoryMap.put("name",names.get(i));
            categories.add(categoryMap);
        }

        //查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //查询规格参数组
        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParam(spu.getCid3());

        //查询特殊的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, null);
        Map<Long,Object> paramMap = new HashMap<>();
        params.forEach(param ->{
            paramMap.put(param.getId(),param.getName());
        });

        //添加spu
        map.put("spu",spu);
        //添加spuDetail
        map.put("spuDetail",spuDetail);
        //添加skus集合
        map.put("skus",skus);
        //添加categories
        map.put("categories",categories);
        //添加品牌
        map.put("brand",brand);
        //添加规格参数组
        map.put("groups",groups);
        //添加特殊规格参数
        map.put("paramMap",paramMap);
        return map;
    }
}
