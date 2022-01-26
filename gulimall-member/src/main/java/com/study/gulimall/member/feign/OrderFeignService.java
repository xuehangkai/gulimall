package com.study.gulimall.member.feign;


import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItem")
    //@RequiresPermissions("order:order:list")
    public R listWithItem(@RequestBody Map<String, Object> params);
}
