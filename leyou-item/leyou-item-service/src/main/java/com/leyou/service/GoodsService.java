package com.leyou.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {

    PageResult<SpuBo> queryGoodsByPage(String key, String saleable, Integer page, Integer rows);

    void saveGoods(SpuBo spuBo);

    List<Sku> querySkusBySpuId(Long id);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    Spu querySpuById(Long id);

    void updateGoods(SpuBo spuBo);

    Sku querySkuBySkuId(Long skuId);
}
