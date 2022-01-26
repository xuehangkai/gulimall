package com.study.gulimall.order.listener;


import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.study.gulimall.order.config.AlipayTemplate;
import com.study.gulimall.order.service.OrderService;
import com.study.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
public class OrderPayedListener {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @PostMapping("/payed/notify")
    public String handleAlipayed( PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {

        System.out.println(payAsyncVo);
        Map<String,String > params=new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for(Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr="";
            for(int i=0;i<values.length;i++){
                valueStr=(i==values.length-1)?valueStr+values[i]:valueStr+values[i]+",";
            }
            //valueStr=new String(valueStr.getBytes("ISO-8859-1"),"utf-8");
            params.put(name,valueStr);
        }
        boolean signVerified= AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),alipayTemplate.getCharset(),alipayTemplate.getSign_type());
        if(signVerified){
            System.out.println("支付宝验签成功");
            String result=orderService.handlePayResult(payAsyncVo);
            return result;
        }else {
            System.out.println("支付宝验签失败");
            return "error";
        }
    }
}
