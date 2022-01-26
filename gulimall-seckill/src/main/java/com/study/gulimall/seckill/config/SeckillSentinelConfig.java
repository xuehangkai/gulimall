package com.study.gulimall.seckill.config;


import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.study.common.exception.BizCodeEnume;
import com.study.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class SeckillSentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        R error = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JSON.toJSONString(error));
    }
}
//@Configuration
//public class SeckillSentinelConfig {
//
//    public SeckillSentinelConfig(){
//        WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
//            @Override
//            public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
//                R error = R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
//                httpServletResponse.setCharacterEncoding("UTF-8");
//                httpServletResponse.setContentType("application/json");
//                httpServletResponse.getWriter().write(JSON.toJSONString(error));
//            }
//        });
//    }
//}
