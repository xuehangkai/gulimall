package com.study.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVo {

     List<MemberReceiveAddressVo> address;

    List<OrderItemVo> items;

    Integer integration;

    BigDecimal total;

    BigDecimal payPrice;

    Map<Long,Boolean> stocks;

    String orderToken;

    public Integer getCount(){
        Integer i=0;
        if(items!=null){
            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }





}
