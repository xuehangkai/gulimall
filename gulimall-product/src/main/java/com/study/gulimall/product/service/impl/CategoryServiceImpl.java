package com.study.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.gulimall.product.service.CategoryBrandRelationService;
import com.study.gulimall.product.vo.Catelog2Vo;
import jdk.nashorn.internal.ir.CallNode;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.product.dao.CategoryDao;
import com.study.gulimall.product.entity.CategoryEntity;
import com.study.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCategorylogPath(Long categoryId) {

        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(categoryId, paths);
        Collections.reverse(parentPath);
        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = "category",key="'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key="'getCatalogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)
    @CachePut
    public void updateCascade(CategoryEntity category) {

        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value={"category"},key="#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("=====");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    @Cacheable(value={"category"},key="#root.method.name")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("+++++");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0l);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, item.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中。。。查询数据库。。。");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中。。。直接返回。。。");
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功。。。");
            //stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
            //stringRedisTemplate.delete("lock");
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)){
//                stringRedisTemplate.delete("lock");
//            }
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String s = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(s, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败。。。等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库。。。");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0l);
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, item.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    //从数据库中查询封装分类数据
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        synchronized (this) {
            return getDataFromDb();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {

        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    private List<Long> findParentPath(Long categoryId, List<Long> paths) {
        paths.add(categoryId);
        CategoryEntity byId = this.getById(categoryId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entityLis = baseMapper.selectList(null);
        //System.out.println(entityLis.toString());
        List<CategoryEntity> levelMenus = entityLis.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getchildrens(menu, entityLis));
                    return menu;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return levelMenus;
    }

    private List<CategoryEntity> getchildrens(CategoryEntity categoryEntity, List<CategoryEntity> entityLis) {
        List<CategoryEntity> childrean = entityLis.stream()
                .filter(categoryEntity1 -> categoryEntity1.getParentCid().equals(categoryEntity.getCatId()))
                .map(entity -> {
                    //System.out.println(entity);
                    entity.setChildren(getchildrens(entity, entityLis));
                    //System.out.println(entity);
                    return entity;
                }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return childrean;
    }

}