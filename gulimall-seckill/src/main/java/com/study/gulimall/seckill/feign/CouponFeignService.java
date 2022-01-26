package com.study.gulimall.seckill.feign;

import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/lates3DaySession")
     R getLates3DaySession();
}
