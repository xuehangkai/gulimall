package com.study.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.constans.ProductConstant;
import com.study.common.to.MemberPrice;
import com.study.common.to.SkuHasStockVo;
import com.study.common.to.SkuReductionTo;
import com.study.common.to.SpuBoundTo;
import com.study.common.to.es.SkuEsModel;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.common.utils.R;
import com.study.gulimall.product.dao.SpuInfoDao;
import com.study.gulimall.product.entity.*;
import com.study.gulimall.product.feign.CouuponFeignService;
import com.study.gulimall.product.feign.SearchFeignService;
import com.study.gulimall.product.feign.WareFeignService;
import com.study.gulimall.product.service.*;
import com.study.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouuponFeignService couuponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info

        SpuInfoEntity infoEntity=new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        System.out.println(infoEntity);
        this.saveBaseSpuInfo(infoEntity);

        //2、保存spu的描述图片 pms_spu_info_desc

        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));

        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(),images);


        //4、保存spu的规格参数 pms_product_attr_value

        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());

            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);

        //5、保存spu的积分信息 gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();

        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couuponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode()!=0){
            log.error("远程保存spu积分信息失败");
        }

        //5、保存当前spu对应的所有sku信息
        //5.1）、sku的基本信息 pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(skus!=null &&skus.size()>0){
            skus.forEach(item->{
                String defaultImg="";
                for(Images img: item.getImages()){
                    if(img.getDefaultImg()==1){
                        defaultImg=img.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息 pms_sku_images
                skuImagesService.saveBatch(imagesEntities);
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息 pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                //5.4）、sku的优惠、满减等信息 gulimall_sms->sms_sku_ladder\ sms_sku_full_reduction\ sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setMemberPrice(item.getMemberPrice());

                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r1 = couuponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode()!=0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        String status=(String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        String brandId=(String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && "0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catalogId=(String) params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId) && "0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        //System.out.println("=============================");
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        //System.out.println("-----------------------------");
        return new PageUtils(page);
    }


    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //List<SkuEsModel> upProducts=new ArrayList<>();
        //SkuEsModel esModel=new SkuEsModel();
        List<SkuInfoEntity> skus= skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long>  searchAttrIds=attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet=new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList=baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item->{
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item,attrs1);
            return attrs1;
        }).collect(Collectors.toList());
        Map<Long, Boolean> stockMap=null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);

            stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常:原因{}",e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts=skus.stream().map(sku->{
            SkuEsModel esModel=new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            if(finalStockMap ==null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            esModel.setHotScore(0L);
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());
            CategoryEntity category= categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode()==0){
            baseMapper.updataSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {

        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();

        SpuInfoEntity spuInfoEntity = getById(spuId);

        return spuInfoEntity;
    }


}