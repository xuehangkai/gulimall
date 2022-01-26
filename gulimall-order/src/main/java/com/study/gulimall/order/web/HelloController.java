package com.study.gulimall.order.web;


import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HelloController {


    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page){

        return page;
    }

}
