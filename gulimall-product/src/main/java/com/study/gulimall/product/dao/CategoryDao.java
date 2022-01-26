package com.study.gulimall.product.dao;

import com.study.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-23 17:02:00
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
