package com.study.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.study.common.exception.BizCodeEnume;
import com.study.gulimall.member.Vo.SocialUser;
import com.study.gulimall.member.Vo.UserLoginVo;
import com.study.gulimall.member.Vo.UserRegistVo;
import com.study.gulimall.member.exception.PhoneExistException;
import com.study.gulimall.member.exception.UserNameExistException;
import com.study.gulimall.member.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.study.gulimall.member.entity.MemberEntity;
import com.study.gulimall.member.service.MemberService;
import com.study.common.utils.PageUtils;
import com.study.common.utils.R;

import javax.annotation.Resource;


/**
 * 会员
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:11:46
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Resource
    private CouponFeignService couponFeignService;


    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser){
        MemberEntity memberEntity= memberService.oauth2Login(socialUser);
        if(memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PAEEWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PAEEWORD_INVAILD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo vo){
        MemberEntity memberEntity= memberService.login(vo);
        if(memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PAEEWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PAEEWORD_INVAILD_EXCEPTION.getMsg());
        }
    }


    @PostMapping("/regist")
    public R regist(@RequestBody UserRegistVo vo){

        try {
            memberService.regist(vo);
        }catch (PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UserNameExistException e){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }


        return R.ok();
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity=new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons=couponFeignService.memberCoupon();
        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
