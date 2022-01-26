package com.study.gulimall.product.service.impl;

import com.study.gulimall.product.entity.AttrEntity;
import com.study.gulimall.product.service.AttrService;
import com.study.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.study.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.product.dao.AttrGroupDao;
import com.study.gulimall.product.entity.AttrGroupEntity;
import com.study.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key=(String)params.get("key");
        //System.out.println(key);
        QueryWrapper<AttrGroupEntity> wrapper= new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and(obj->
            {
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if(catelogId==0){
            IPage<AttrGroupEntity> page =this.page(new Query<AttrGroupEntity>().getPage(params),wrapper);
            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page =this.page(new Query<AttrGroupEntity>().getPage(params),wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item,attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;

        }).collect(Collectors.toList());
        return collect;

    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {

        AttrGroupDao baseMapper = this.getBaseMapper();
        List<SkuItemVo.SpuItemAttrGroupVo> vos=baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);

        return vos;
    }
}