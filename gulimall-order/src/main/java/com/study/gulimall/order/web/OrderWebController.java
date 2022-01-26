package com.study.gulimall.order.web;


import com.study.common.exception.NoStockException;
import com.study.gulimall.order.service.OrderService;
import com.study.gulimall.order.vo.OrderConfirmVo;
import com.study.gulimall.order.vo.OrderSubmitVo;
import com.study.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;


    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo= orderService.confirmOrder();
        //System.out.println(confirmVo);
        model.addAttribute("orderConfirmData",confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model , RedirectAttributes redirectAttributes){

        try {
            SubmitOrderResponseVo responseVo= orderService.submitOrder(vo);
            if(responseVo.getCode()==0){
                System.out.println(responseVo);
                model.addAttribute("submitOrderRes",responseVo);
                return "/pay";
            }else {
                String msg="下单失败";
                switch (responseVo.getCode()){
                    case 1: msg+="订单信息过期，请重新提交";break;
                    case 2: msg+="订单商品价格发生变化，请确认后再次提交";break;
                    case 3: msg+="商品库存不足";
                }
                System.out.println(msg);
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }catch (NoStockException e){
            System.out.println(e.getMsg()+"===");
            redirectAttributes.addFlashAttribute("msg",e.getMsg());
            return "redirect:http://order.gulimall.com/toTrade";
        }




    }


}
