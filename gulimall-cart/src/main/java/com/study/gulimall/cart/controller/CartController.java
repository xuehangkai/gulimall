package com.study.gulimall.cart.controller;


import com.study.common.constans.AuthServerConstant;
import com.study.gulimall.cart.interceptor.CartInterceptor;
import com.study.gulimall.cart.service.CartService;
import com.study.gulimall.cart.vo.Cart;
import com.study.gulimall.cart.vo.CartItem;
import com.study.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }


    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num")  Integer num, RedirectAttributes ra) throws ExecutionException, InterruptedException {
            CartItem cartItem= cartService.addToCart(skuId,num);
            ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,Model model){
        CartItem cartItem=cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart=cartService.getCart();
        //System.out.println(cart.toString());
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

}
