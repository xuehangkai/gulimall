package com.study.gulimall.order.vo;


import com.study.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {


    private OrderEntity order;
    private Integer code;
}
