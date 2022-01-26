package com.study.gulimall.order.web;


import com.alipay.api.AlipayApiException;
import com.study.gulimall.order.config.AlipayTemplate;
import com.study.gulimall.order.service.OrderService;
import com.study.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {


    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;


    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);
//        payVo.setBody();
//        payVo.setOut_trade_no();
//        payVo.setSubject();
//        payVo.setTotal_amount();
        String pay = alipayTemplate.pay(payVo);
        //System.out.println(pay);
        return pay;
    }

}
