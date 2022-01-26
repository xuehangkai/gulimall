package com.study.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.study.common.utils.R;
import com.study.gulimall.product.entity.SkuImagesEntity;
import com.study.gulimall.product.entity.SpuInfoDescEntity;
import com.study.gulimall.product.feign.SeckillFeignService;
import com.study.gulimall.product.service.*;
import com.study.gulimall.product.vo.SeckillInfoVo;
import com.study.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.product.dao.SkuInfoDao;
import com.study.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;
import sun.plugin.javascript.navig.LinkArray;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {

        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key =(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        String catalogId =(String) params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId) && "0".equalsIgnoreCase(catalogId)){

            wrapper.eq("catalog_id",catalogId);
        }
        String brandId =(String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && "0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String min =(String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            wrapper.ge("price",min);
        }
        String max =(String) params.get("max");

        if(!StringUtils.isEmpty(max) ){

            try {
                BigDecimal bigDecimal = new BigDecimal(max);

                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    wrapper.le("price",max);
                }
            }catch (Exception e){
                System.out.println(e);
            }

        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );




        return new PageUtils(page);

    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));


        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo=new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemVo.ItemSaleAttrsVo> saleAttrsVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            //System.out.println(saleAttrsVos);
            skuItemVo.setSaleAttr(saleAttrsVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemVo.SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if(r.getCode()==0){
                SeckillInfoVo data = r.getData(new TypeReference<SeckillInfoVo>() {});
                skuItemVo.setSeckillInfoVo(data);
            }
        }, executor);

        CompletableFuture.allOf(infoFuture,saleAttrFuture,descFuture,baseAttrFuture,imageFuture,seckillFuture).get();

        return skuItemVo;
    }

}