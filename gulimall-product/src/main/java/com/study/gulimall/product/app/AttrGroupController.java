package com.study.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.study.gulimall.product.entity.AttrEntity;
import com.study.gulimall.product.service.AttrAttrgroupRelationService;
import com.study.gulimall.product.service.AttrService;
import com.study.gulimall.product.service.CategoryService;
import com.study.gulimall.product.vo.AttrGroupRelationVo;
import com.study.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.study.gulimall.product.entity.AttrGroupEntity;
import com.study.gulimall.product.service.AttrGroupService;
import com.study.common.utils.PageUtils;
import com.study.common.utils.R;



/**
 * 属性分组
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-23 17:02:00
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;


    @Autowired
    private AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;


    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId){

        List<AttrGroupWithAttrsVo> vos=attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);

    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.saveBatch(vos);


        return R.ok();

    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities=attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entities);

    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page=attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);

    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId){
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page=attrGroupService.queryPage(params,catelogId);


        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		Long categoryId=attrGroup.getCatelogId();
        Long[] path= categoryService.findCategorylogPath(categoryId);

		attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
