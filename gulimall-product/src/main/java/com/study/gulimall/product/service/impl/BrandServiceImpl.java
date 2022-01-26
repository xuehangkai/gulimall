package com.study.gulimall.product.service.impl;

import com.study.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.product.dao.BrandDao;
import com.study.gulimall.product.entity.BrandEntity;
import com.study.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> brandEntityQueryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            brandEntityQueryWrapper.eq("brand_id",key).or().like("name",key);
        }


        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                brandEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {

        List<BrandEntity> brandEntities = baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
        return brandEntities;
    }


}