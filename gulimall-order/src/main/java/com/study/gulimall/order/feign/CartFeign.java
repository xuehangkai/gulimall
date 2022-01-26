package com.study.gulimall.order.feign;


import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.study.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeign {

    @GetMapping("/currentUserCartItems")
     List<OrderItemVo> getCurrentUserCartItems();
}
