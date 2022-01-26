package com.study.gulimall.seckill.interceptor;


import com.study.common.constans.AuthServerConstant;
import com.study.common.vo.MemberEntityVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntityVo> toThreadLocal=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean match = new AntPathMatcher().match("/kill", request.getRequestURI());
        if(match){
            HttpSession session = request.getSession();
            MemberEntityVo member = (MemberEntityVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            //System.out.println(member.toString());
            if(member!=null){
                toThreadLocal.set(member);
                return true;
            }else {
                session.setAttribute("msg","请先进行登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
