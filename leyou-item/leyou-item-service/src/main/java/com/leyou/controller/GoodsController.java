package com.leyou.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.service.impl.GoodsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller

public class GoodsController {

    @Autowired
    private GoodsServiceImpl goodsService;

    /**
     * 商品分页查询
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> queryGoodsByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) String saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    ){
        PageResult<SpuBo> result = this.goodsService.queryGoodsByPage(key,saleable,page,rows);
        if(CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuid查询sku集合
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long id){
        List<Sku> skuList = this.goodsService.querySkusBySpuId(id);
        if(CollectionUtils.isEmpty(skuList)){
             return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skuList);
    }


    /**
     * 根据spuId查询spuDetail详情
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpuId(spuId);
        if(spuDetail == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spiId查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if(spu == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 根据skuId查询sku
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId){
        Sku sku = this.goodsService.querySkuBySkuId(skuId);
        if(sku == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }
}
