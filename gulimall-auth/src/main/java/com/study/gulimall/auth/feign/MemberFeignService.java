package com.study.gulimall.auth.feign;

import com.study.common.utils.R;
import com.study.gulimall.auth.Vo.SocialUser;
import com.study.gulimall.auth.Vo.UserLoginVo;
import com.study.gulimall.auth.Vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {


    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
     R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUser socialUser);
}
