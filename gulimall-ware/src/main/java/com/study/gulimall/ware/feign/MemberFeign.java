package com.study.gulimall.ware.feign;


import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeign {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
     R info(@PathVariable("id") Long id);

}
