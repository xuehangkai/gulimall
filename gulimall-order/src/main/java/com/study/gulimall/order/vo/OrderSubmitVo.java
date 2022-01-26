package com.study.gulimall.order.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

@Data
public class OrderSubmitVo {

    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private String note;


}
