package com.study.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.gulimall.member.Vo.SocialUser;
import com.study.gulimall.member.Vo.UserLoginVo;
import com.study.gulimall.member.Vo.UserRegistVo;
import com.study.gulimall.member.entity.MemberEntity;
import com.study.gulimall.member.exception.PhoneExistException;
import com.study.gulimall.member.exception.UserNameExistException;

import java.util.Map;

/**
 * 会员
 *
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-21 22:11:46
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(UserRegistVo vo);

    void checkPhoneUnique(String email) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameExistException;


    MemberEntity login(UserLoginVo vo);

    MemberEntity oauth2Login(SocialUser socialUser);
}

