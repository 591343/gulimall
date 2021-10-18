package com.xiaochen.cart.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xiaochen.cart.feign.ProductFeignService;
import com.xiaochen.cart.interceptor.CartInterceptor;
import com.xiaochen.cart.service.CartService;
import com.xiaochen.cart.to.UserInfoTo;
import com.xiaochen.cart.vo.Cart;
import com.xiaochen.cart.vo.CartItem;
import com.xiaochen.cart.vo.SkuInfoVo;
import com.xiaochen.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    private final String CART_PREFIX = "gulimall:cart:";


    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            CartItem cartItem = new CartItem();
            // 异步编排
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                // 1. 远程查询当前要添加的商品的信息
                R skuInfo = productFeignService.info(skuId);
                SkuInfoVo sku = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                // 2. 添加新商品到购物车
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setImage(sku.getSkuDefaultImg());
                cartItem.setPrice(sku.getPrice());
                cartItem.setTitle(sku.getSkuTitle());
                cartItem.setSkuId(skuId);
            }, executor);

            // 3. 远程查询sku组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(values);
            }, executor);
            CompletableFuture.allOf(getSkuInfo, getSkuSaleAttrValues).get();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }else{
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /**
     * 获取到我们要操作的购物车 [已经包含用户前缀 只需要带上用户id 或者临时id 就能对购物车进行操作]
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        // 1. 这里我们需要知道操作的是离线购物车还是在线购物车
        String cartKey = CART_PREFIX;
        if(userInfoTo.getUserId() != null){
            log.debug("\n用户 [" + userInfoTo.getUsername() + "] 正在操作购物车");
            // 已登录的用户购物车的标识
            cartKey += userInfoTo.getUserId();
        }else{
            log.debug("\n临时用户 [" + userInfoTo.getUserKey() + "] 正在操作购物车");
            // 未登录的用户购物车的标识
            cartKey += userInfoTo.getUserKey();
        }
        // 绑定这个 key 以后所有对redis 的操作都是针对这个key
        return stringRedisTemplate.boundHashOps(cartKey);
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        cartOps.keys();
        return JSON.parseObject(o, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        String tempCartKey=CART_PREFIX+userInfoTo.getUserKey(); //临时用户的键


        if(userInfoTo.getUserId()!=null){   //用户已登录
            String cartKey=CART_PREFIX+userInfoTo.getUserId(); //誊录用户的键
            List<CartItem> cartItems = getCartItems(tempCartKey);
            if(cartItems!=null){
                // 1.2 临时购物车有数据 则进行合并
                log.info("\n[" + userInfoTo.getUsername() + "] 的购物车已合并");
                for (CartItem cartItem : cartItems) {
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //清空临时购物车
                clearCart(tempCartKey);
            }
            List<CartItem> cartItems1 = getCartItems(cartKey);
            cart.setItems(cartItems1);
        }else {    //用户未登陆
            // 2. 没登录 获取临时购物车的所有购物项
            cart.setItems(getCartItems(tempCartKey));
        }
        return cart;
    }

    /**
     * 查询所有临时用户的购物项并返回
     * @param tempCartKey
     * @return
     */
    private List<CartItem> getCartItems(String tempCartKey) {
        BoundHashOperations<String, Object, Object> cartItems = stringRedisTemplate.boundHashOps(tempCartKey);
        List<Object> values = cartItems.values();
        if(values!=null&&values.size()>0){
            return values.stream().map(obj -> JSON.parseObject((String)obj, CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey){
        stringRedisTemplate.delete(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteItem(Long skuId) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        Long delete = cartOps.delete(skuId.toString());
        if(userInfoTo.getUserId() != null){
            log.debug("\n用户 [" + userInfoTo.getUsername() + "] 已删除"+delete+"号商品");
            // 已登录的用户购物车的标识

        }else{
            log.debug("\n临时用户 [" + userInfoTo.getUserKey() + "] 已删除"+delete+"号商品");
            // 未登录的用户购物车的标识

        }
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo==null){
            return null;
        }

        String cartKey=CART_PREFIX+userInfoTo.getUserId(); //誊录用户的键
        List<CartItem> cartItems = getCartItems(cartKey);
        if(cartItems!=null&&cartItems.size()!=0){
            cartItems = cartItems.stream().filter(CartItem::getCheck).map(item->{
                BigDecimal price = productFeignService.skuPrice(item.getSkuId());
                //更新为最新价格
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());
        }else {
            return null;
        }

        return cartItems;
    }
}
