package com.study.gulimall.ware.exception;

public class NoStockException extends RuntimeException{
    private Long skuId;

    public NoStockException(Long skuId) {

        super("商品id:"+skuId+"没有足够的库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
