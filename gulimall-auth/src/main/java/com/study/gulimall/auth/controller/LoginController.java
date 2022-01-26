package com.study.gulimall.auth.controller;


import com.alibaba.fastjson.TypeReference;
import com.study.common.constans.AuthServerConstant;
import com.study.common.exception.BizCodeEnume;
import com.study.common.utils.R;
import com.study.common.vo.MemberEntityVo;
import com.study.gulimall.auth.Vo.UserLoginVo;
import com.study.gulimall.auth.Vo.UserRegistVo;
import com.study.gulimall.auth.feign.MemberFeignService;
import com.study.gulimall.auth.feign.ThirdPartFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;


    @GetMapping("/login.html")
    public String loginPage(HttpSession session){

        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute==null){
            return "login";
        }else {
            return "redirect:http://gulimall.com";
        }



    }


    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes, HttpSession session){
        R login = memberFeignService.login(vo);

        if(login.getCode()==0){
            MemberEntityVo data = login.getData("data", new TypeReference<MemberEntityVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors,",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis()-l<60000){
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //String substring = UUID.randomUUID().toString().substring(0, 6);
        String code =(int)(Math.random()*9+1)*100000+"_"+System.currentTimeMillis();
        //System.out.println(substring);
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,5, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }
//    @PostMapping("/re")
//    public String re(@ModelAttribute("errors") Map<String, String> errors,Model model){
//
//        model.addAttribute("errors",errors);
//        return "redirect:http://auth.gulimall.com/reg.html";
//    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){

        if(result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage,(entity1,entity2) -> entity1));
            System.out.println(errors.toString());
            //redirectAttributes.addAttribute("errors,",errors);
            redirectAttributes.addFlashAttribute("errors,",errors);
            //model.addAttribute("errors,",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

        if(!StringUtils.isEmpty(s)){
            if(code.equals(s.split("_")[0])){
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                R r = memberFeignService.regist(vo);
                if(r.getCode()==0){
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors,",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors,",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors,",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }


    }

}
