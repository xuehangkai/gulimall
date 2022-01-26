package com.study.gulimall.product.app;

import java.util.Arrays;
import java.util.List;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.gulimall.product.entity.CategoryEntity;
import com.study.gulimall.product.service.CategoryService;
import com.study.common.utils.R;



/**
 * 商品三级分类
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-23 17:02:00
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类及子分类以树形结构组装
     */
    @RequestMapping("/list/tree")
    //@RequiresPermissions("product:category:list")
    public R list(){

        List<CategoryEntity> entityLis=categoryService.listWithTree();

        return R.ok().put("data", entityLis);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);

        return R.ok();
    }

    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){


		//categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenusByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
