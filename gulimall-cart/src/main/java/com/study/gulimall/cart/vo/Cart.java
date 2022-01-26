package com.study.gulimall.cart.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class Cart {

    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduces=new BigDecimal("0");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        int count=0;
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal setTotalAmount() {
        BigDecimal amount=new BigDecimal("0");
        if (items!=null && items.size()>0){
            for (CartItem item : items) {
                if(item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount=amount.add(totalPrice);
                }
            }
        }

        BigDecimal subtract = amount.subtract(this.getReduces());
        this.totalAmount=subtract;
        return subtract;
    }


    public BigDecimal getReduces() {
        return reduces;
    }

    public void setReduces(BigDecimal reduces) {
        this.reduces = reduces;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "items=" + items +
                ", countNum=" + countNum +
                ", countType=" + countType +
                ", totalAmount=" + totalAmount +
                ", reduces=" + reduces +
                '}';
    }
}
