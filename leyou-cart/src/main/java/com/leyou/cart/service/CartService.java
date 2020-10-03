package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;

import java.util.List;

public interface CartService {

    void addCart(Cart cart);

    List<Cart> queryCart();

    void updateNum(Cart cart);

    void deleteCart(String skuId);

    void mergeCarts(List<Cart> carts);
}
