package com.study.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.study.common.constans.ProductConstant;
import com.study.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.study.gulimall.product.dao.AttrGroupDao;
import com.study.gulimall.product.dao.CategoryDao;
import com.study.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.study.gulimall.product.entity.AttrGroupEntity;
import com.study.gulimall.product.entity.CategoryEntity;
import com.study.gulimall.product.service.CategoryService;
import com.study.gulimall.product.vo.AttrGroupRelationVo;
import com.study.gulimall.product.vo.AttrResVo;
import com.study.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.product.dao.AttrDao;
import com.study.gulimall.product.entity.AttrEntity;
import com.study.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );
        return new PageUtils(page);
    }
    @Override
    @Transactional
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
        if(attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId()!=null){
            AttrAttrgroupRelationEntity relationEntity=new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(relationEntity);
        }
        }
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if(catelogId!=0){
            attrEntityQueryWrapper.eq("catelog_id",catelogId);
        }
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            //System.out.println(key);
            attrEntityQueryWrapper.and((wrapper)->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                attrEntityQueryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResVo> respVos = records.stream().map((attrEntity) -> {
            AttrResVo attrResVo = new AttrResVo();
            BeanUtils.copyProperties(attrEntity, attrResVo);
            if("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity attrId = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null &&attrId.getAttrGroupId()!=null) {
                    Long attrGroupId = attrId.getAttrGroupId();
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                    System.out.println(attrGroupEntity.getAttrGroupName());
                    attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResVo.setCatelogName(categoryEntity.getName());
            }
            return attrResVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    @Cacheable(value = "attr",key = "'attrinfo:#root.args[0]'")
    public AttrResVo getAttrInfo(Long attrId) {
        AttrResVo resVo=new AttrResVo();
        AttrEntity attrEntity=this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,resVo);
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attrgroupRelation = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(attrgroupRelation!=null){
                resVo.setAttrGroupId(attrgroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                if(attrGroupEntity!=null){
                    resVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        Long cateLogId=attrEntity.getCatelogId();
        Long[] categorylogPath = categoryService.findCategorylogPath(cateLogId);

        resVo.setCatelogPath(categorylogPath);
        CategoryEntity categoryEntity= categoryDao.selectById(cateLogId);
        if(categoryEntity!=null){
            resVo.setCatelogName(categoryEntity.getName());
        }
        return resVo;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            AttrAttrgroupRelationEntity attrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrgroupRelationEntity.setAttrId(attr.getAttrId());
            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0) {

                relationDao.update(attrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            } else {
                relationDao.insert(attrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if(attrIds==null||attrIds.size()==0){
            return null;
        }

        List<AttrEntity> attrEntities = this.listByIds(attrIds);

        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(entities);
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {

        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long attrGroupId = attrGroupEntity.getCatelogId();
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", attrGroupId));
        List<Long> collect = group.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", attrGroupId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(attrIds!=null && attrIds.size()>0){
            System.out.println(attrIds);
            wrapper.notIn("attr_id", attrIds);
        }

        //List<AttrEntity> attrEntities = this.baseMapper.selectList(wrapper);
        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {


        return baseMapper.selectSearchAttrIds(attrIds);

    }
}