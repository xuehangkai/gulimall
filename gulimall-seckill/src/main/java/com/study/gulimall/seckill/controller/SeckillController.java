package com.study.gulimall.seckill.controller;


import com.study.common.utils.R;
import com.study.gulimall.seckill.service.SeckillService;
import com.study.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos= seckillService.getCurrentSeckillSkus();
        return  R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo seckillSkuRedisTo=seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(seckillSkuRedisTo);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId , @RequestParam("key")String key , @RequestParam("num")Integer num, Model model){
        String orderSn=seckillService.seckill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
