package com.study.gulimall.ware.feign;


import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-gateway")
public interface ProductFeignService {

    @RequestMapping("/api/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
