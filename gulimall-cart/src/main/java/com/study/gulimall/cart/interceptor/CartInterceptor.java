package com.study.gulimall.cart.interceptor;


import com.study.common.constans.AuthServerConstant;
import com.study.common.constans.CartConstant;
import com.study.common.vo.MemberEntityVo;
import com.study.gulimall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;



public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> toThreadLocal=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        //System.out.println(member.toString());
        if(member!=null){
            userInfoTo.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null && cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if(name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String s = UUID.randomUUID().toString();
            userInfoTo.setUserKey(s);
        }

        toThreadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if(!toThreadLocal.get().isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, toThreadLocal.get().getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
