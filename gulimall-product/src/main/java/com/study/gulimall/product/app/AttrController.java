package com.study.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.study.gulimall.product.entity.ProductAttrValueEntity;
import com.study.gulimall.product.service.ProductAttrValueService;
import com.study.gulimall.product.vo.AttrResVo;
import com.study.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.study.gulimall.product.service.AttrService;
import com.study.common.utils.PageUtils;
import com.study.common.utils.R;



/**
 * 商品属性
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-23 17:02:00
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;



    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> entities= productAttrValueService.baseAttrlistforspu(spuId);

        return R.ok().put("data",entities);
    }


    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType")String attrType){


        PageUtils page =attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page",page);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		//AttrEntity attr = attrService.getById(attrId);
        AttrResVo attrRespvo=attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespvo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId")Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){

        productAttrValueService.updateSpuAttr(spuId,entities);

        return R.ok();

    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
