package com.study.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.common.utils.R;
import com.study.gulimall.cart.feign.ProductFeignService;
import com.study.gulimall.cart.interceptor.CartInterceptor;
import com.study.gulimall.cart.service.CartService;
import com.study.gulimall.cart.vo.Cart;
import com.study.gulimall.cart.vo.CartItem;
import com.study.gulimall.cart.vo.SkuInfoVo;
import com.study.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;


    @Autowired
    ProductFeignService productFeignService;

    private final String CART_PREFIX="gulimall:cart:";

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res= (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setCheck(true);
                cartItem.setSkuId(data.getSkuId());
                cartItem.setPrice(data.getPrice());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setCount(num);
                cartItem.setTotalPrice();
                //System.out.println(cartItem.setTotalPrice());
            },executor);
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);
            CompletableFuture.allOf(getSkuInfo,getSkuSaleAttrValues).get();
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),jsonString);
            return cartItem;
        }else {
            CartItem cartItem = new CartItem();
            cartItem = JSON.parseObject(res, CartItem.class);
            System.out.println(cartItem.toString());
            cartItem.setCount(cartItem.getCount()+num);
            cartItem.setTotalPrice();
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps= getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        cartItem.setTotalPrice();
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        if(userInfoTo.getUserId()!=null){
            String cartKey =CART_PREFIX+ userInfoTo.getUserId();
            String tempCartKey=CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if(tempCartItems!=null){
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                clearCart(tempCartKey);
            }
            List<CartItem> cartItems = getCartItems(cartKey);
            for (CartItem cartItem : cartItems) {
                cartItem.setTotalPrice();
            }
            cart.setItems(cartItems);
            cart.setTotalAmount();
            cart.setReduces(new BigDecimal("0"));
        }else {
            String cartKey =CART_PREFIX+ userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            for (CartItem cartItem : cartItems) {
                cartItem.setTotalPrice();
            }
            cart.setItems(cartItems);
            cart.setTotalAmount();
            cart.setReduces(new BigDecimal("0"));
        }
        //System.out.println(cart.toString());
        cart.setTotalAmount();



        return cart;
    }

    private  List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if(values!=null && values.size()>0){
            List<CartItem> collect = values.stream().map(obj -> {
                String str= (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return  collect;
        }
        return  null;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();

        String cartKey="";
        if(userInfoTo.getUserId()!=null){
            cartKey=CART_PREFIX+userInfoTo.getUserId();
        }else {
            cartKey=CART_PREFIX+userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
    @Override
    public void clearCart(String cartKey){
        Boolean delete = redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),jsonString);


    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),jsonString);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();
        System.out.println(userInfoTo);
        if(userInfoTo.getUserId()==null){
            return null;
        }else {
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck())
                    .map(item->{
                        item.setPrice(productFeignService.getPrice(item.getSkuId()));
                        item.setTotalPrice();
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
    }
}
