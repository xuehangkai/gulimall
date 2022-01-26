package com.study.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.gulimall.ware.entity.WareInfoEntity;
import com.study.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:13:32
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long id);
}

