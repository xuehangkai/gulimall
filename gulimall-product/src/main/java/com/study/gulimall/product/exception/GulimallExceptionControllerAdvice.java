package com.study.gulimall.product.exception;


import com.study.common.exception.BizCodeEnume;
import com.study.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ResponseBody
@RestControllerAdvice(basePackages = "com.study.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("数据异常{}，异常类型:{}",e.getMessage(),e.getClass());

        BindingResult result=e.getBindingResult();
        Map<String,String> errorMap=new HashMap<>();
        result.getFieldErrors().forEach((item)-> {
            String message = item.getDefaultMessage();
            String field = item.getField();
            errorMap.put(field, message);

        });
        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),BizCodeEnume.VAILD_EXCEPTION.getMsg()).put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }

}
