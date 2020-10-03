package com.leyou.cart.service.Impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "leyou:cart:uid:";

    @Autowired
    private GoodsClient goodsClient;

    @Override
    public void addCart(Cart cart) {
        //获取登录的用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //redis的key值
        //获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX+userInfo.getId());
        //查询是否存在
        Integer num = cart.getNum();
        String key = cart.getSkuId().toString();
        if(hashOps.hasKey(key)){
            //如果已经存在，更新购物车数据
            String json = hashOps.get(key).toString();
            cart = JsonUtils.parse(json, Cart.class);
            //修改购物车数量
            cart.setNum(cart.getNum()+num);
        }else {
            //如果不存在，新增购物车数据
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setUserId(userInfo.getId());
            cart.setImage(StringUtils.isBlank(sku.getImages())? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setTitle(sku.getTitle());
            cart.setPrice(sku.getPrice());
        }
        // 将购物车数据写入redis
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    @Override
    public void mergeCarts(List<Cart> carts) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX+userInfo.getId();
        //判断是否存在购物车
        if(!this.redisTemplate.hasKey(key)){
            //不存在，直接返回
            return;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //遍历集合
        for (Cart cart : carts) {
            Integer num = cart.getNum();
            String carKey = cart.getSkuId().toString();
            if(hashOps.hasKey(carKey)){
                //如果已经存在，更新购物车数据
                String json = hashOps.get(carKey).toString();
                cart = JsonUtils.parse(json, Cart.class);
                //修改购物车数量
                cart.setUserId(userInfo.getId());
                cart.setNum(cart.getNum()+num);
                // 将购物车数据写入redis
                hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
            }else {
                cart.setUserId(userInfo.getId());
                //如果不存在，新增购物车数据
                hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
            }
        }
    }

    @Override
    public List<Cart> queryCart() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX+userInfo.getId();
        //判断是否存在购物车
        if(!this.redisTemplate.hasKey(key)){
            //不存在，直接返回
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        //判断购物车是否有数据
        if(CollectionUtils.isEmpty(carts)){
            //如果没有数据，直接返回
            return null;
        }
        return carts.stream().map(cart -> JsonUtils.parse(cart.toString(),Cart.class)).collect(Collectors.toList());
    }

    @Override
    public void updateNum(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX+userInfo.getId();
        //判断是否存在购物车
        if(!this.redisTemplate.hasKey(key)){
            //不存在，直接返回
            return;
        }
        Integer num = cart.getNum();
        //获取购物车信息
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JsonUtils.parse(cartJson, Cart.class);
        //更新购物车数量
        cart.setNum(num);
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));

    }

    @Override
    public void deleteCart(String skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX+userInfo.getId();
        //判断是否存在购物车
        if(!this.redisTemplate.hasKey(key)){
            //不存在，直接返回
            return;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }

}
