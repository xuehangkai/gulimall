package com.study.gulimall.auth.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.common.constans.AuthServerConstant;
import com.study.common.utils.HttpUtils;
import com.study.common.utils.R;
import com.study.common.vo.MemberEntityVo;
import com.study.gulimall.auth.Vo.SocialUser;
import com.study.gulimall.auth.feign.MemberFeignService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Controller
public class OAuth2Controller {


    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) {
        //System.out.println(code);
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "3191597893");
        map.put("client_secret", "93cdf42e878e652c2e740cedb6e09fb1");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        try {
            HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), null, map);
            //System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
            //System.out.println(response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity());
                SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
                //System.out.println(socialUser.toString());
                //System.out.println(json);
                R r = memberFeignService.oauth2Login(socialUser);
                if (r.getCode() == 0) {
                    MemberEntityVo data = r.getData("data", new TypeReference<MemberEntityVo>() {});
                    System.out.println(data.toString());
                    session.setAttribute(AuthServerConstant.LOGIN_USER,data);
                    return "redirect:http://gulimall.com";

                } else {
                    return "redirect:http://auth.gulimall.com/login.html";
                }


            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:http://gulimall.com";
    }
}
