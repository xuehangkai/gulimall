package com.study.gulimall.member.feign;

import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("gulimall-coupon")
@Component
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupon();
}
