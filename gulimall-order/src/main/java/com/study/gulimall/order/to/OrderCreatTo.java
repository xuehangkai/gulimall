package com.study.gulimall.order.to;


import com.study.gulimall.order.entity.OrderEntity;
import com.study.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreatTo {

    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;


}
