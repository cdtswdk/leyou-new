package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.Impl.CartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartServiceImpl cartService;

    /**
     * 添加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCart(){
        List<Cart> carts = this.cartService.queryCart();
        if(CollectionUtils.isEmpty(carts)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 修改商品数量
     * @param cart
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除商品
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId")String skuId){
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 合并购物车
     * @param carts
     * @return
     */
    @PutMapping("/carts")
    public ResponseEntity<Void> mergeCarts(@RequestBody List<Cart> carts){
        this.cartService.mergeCarts(carts);
        return ResponseEntity.ok().build();
    }
}
