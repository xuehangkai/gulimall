package com.study.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.study.common.utils.R;
import com.study.gulimall.ware.feign.MemberFeign;
import com.study.gulimall.ware.vo.FareVo;
import com.study.gulimall.ware.vo.MemberReceiveAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.ware.dao.WareInfoDao;
import com.study.gulimall.ware.entity.WareInfoEntity;
import com.study.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeign memberFeign;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key=(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("name",key).or().like("address",key).or().like("areacode",key);
            });
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long id) {

        FareVo fareVo = new FareVo();
        R info = memberFeign.info(id);
        MemberReceiveAddressVo data = info.getData("memberReceiveAddress",new TypeReference<MemberReceiveAddressVo>() {
        });

        if(data!=null){
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}