package com.study.gulimall.cart.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


public class CartItem {


    private Long skuId;
    private Boolean check=true;
    private String title;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal setTotalPrice() {
        this.totalPrice=this.price.multiply(new BigDecimal(""+this.count));

        return this.price.multiply(new BigDecimal(""+this.count));
    }


    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "skuId=" + skuId +
                ", check=" + check +
                ", title='" + title + '\'' +
                ", skuAttr=" + skuAttr +
                ", price=" + price +
                ", count=" + count +
                ", totalPrice=" + totalPrice +
                ", image='" + image + '\'' +
                '}';
    }
}
