package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;
import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.constant.LyConstant;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhongliang
 * @date 2021/5/17 15:15
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        // 在拦截器中已经从gateway传过来的request中获取到了
        Long userId = UserHolder.getUserId();
        // 得到当前用户在redis中的购物数据的key
        String redisKey = LyConstant.CART_PRE+userId;
        // 得到当前用户原来的购物车列表
        BoundHashOperations<String, String, String> redisHash = redisTemplate.boundHashOps(redisKey);
        //得到当前新添加的购物车对象在redis中的hashkey   redisKey当前用户id hashKey是当前skuId
        String hashKey = cart.getSkuId().toString();
        // 判断新添加的购物车对象是否在其中
        if(redisHash.hasKey(hashKey)){
            //得到redis中原来与新添加的skuid一致的购物车对象
            Cart redisCart = JsonUtils.toBean(redisHash.get(hashKey), Cart.class);
            //如果在，合并数量，此刻新保存的购物车对象，已经包含了原来对应sku购物车对象的数量
            cart.setNum(cart.getNum()+redisCart.getNum());
        }
        //不在，无需操作  直接保存新添加的cart即可
        //再次保存购物车 再次保存，没有hashKey会新加，有会覆盖
        redisHash.put(hashKey, JsonUtils.toString(cart));
    }

    /**
     * 在redis中购物车的数据各式为<redisKey, <hashKey, cartStr>>
     *     现在只要cartStr的集合
     * @return
     */
    public List<Cart> queryCarts() {
        // 获取用户id
        Long userId = UserHolder.getUserId();
        //得到当前用户在redis中的购物车的key
        String redisKey = LyConstant.CART_PRE+userId;

        // 先得到原来当前用户的购物车列表
        BoundHashOperations<String, String, String> redisCart = redisTemplate.boundHashOps(redisKey);
        if (redisCart!=null) {
            Map<String, String> entries = redisCart.entries();
            List<Cart> carts = entries.values()
                    .stream()
                    .map(cartStr -> JsonUtils.toBean(cartStr, Cart.class))
                    .collect(Collectors.toList());

            return carts;
        }

        return null;
    }

    public void addCarts(List<Cart> carts) {
        for (Cart cart : carts) {
            addCart(cart);
        }
    }
}
