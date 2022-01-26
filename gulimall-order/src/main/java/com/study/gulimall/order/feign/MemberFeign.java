package com.study.gulimall.order.feign;


import com.study.gulimall.order.vo.MemberReceiveAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeign {

    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    public List<MemberReceiveAddressVo> getAddress(@PathVariable("memberId") Long memberId);


}
