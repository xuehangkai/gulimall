package com.study.gulimall.order.feign;


import com.study.common.utils.R;
import com.study.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
     R getFare(@RequestParam("addrId") Long id);

    @PostMapping("/ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
