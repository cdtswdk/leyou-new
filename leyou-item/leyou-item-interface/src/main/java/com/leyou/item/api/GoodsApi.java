package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {

    /**
     * 商品分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    PageResult<SpuBo> queryGoodsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) String saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );

    /**
     * 根据spuid查询sku集合
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkusBySpuId(@RequestParam("id") Long id);

    /**
     * 根据spuId查询spuDetail详情
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spiId查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id")Long id);

    /**
     * 根据skuId查询sku
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    Sku querySkuBySkuId(@PathVariable("skuId")Long skuId);
}
