package com.study.gulimall.product.vo;

import com.study.gulimall.product.entity.SkuImagesEntity;
import com.study.gulimall.product.entity.SkuInfoEntity;
import com.study.gulimall.product.entity.SpuInfoDescEntity;
import com.study.gulimall.product.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

     SkuInfoEntity info;

     boolean hasStock=true;

     List<SkuImagesEntity> images;

    List<ItemSaleAttrsVo> saleAttr;

     SpuInfoDescEntity desp;

     List<SpuItemAttrGroupVo> groupAttrs;

     SeckillInfoVo seckillInfoVo;

     @Data
     public static class ItemSaleAttrsVo{
         private Long attrId;
         private String attrName;
         private List<AttrValueWithSkuIdVo> attrValues;
     }
    @Data
    public static class SpuItemAttrGroupVo{
         private String groupName;
         private List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}
