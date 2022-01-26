package com.study.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.study.common.utils.HttpUtils;
import com.study.gulimall.member.Vo.SocialUser;
import com.study.gulimall.member.Vo.UserLoginVo;
import com.study.gulimall.member.Vo.UserRegistVo;
import com.study.gulimall.member.dao.MemberLevelDao;
import com.study.gulimall.member.entity.MemberLevelEntity;
import com.study.gulimall.member.exception.PhoneExistException;
import com.study.gulimall.member.exception.UserNameExistException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;

import com.study.gulimall.member.dao.MemberDao;
import com.study.gulimall.member.entity.MemberEntity;
import com.study.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(UserRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());


        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String email) throws PhoneExistException {
        Integer mobile = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", email));
        if (mobile > 0) {
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity == null) {
            return null;
        } else {
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, password1);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity oauth2Login(SocialUser socialUser) {

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));

        if (memberEntity != null) {
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());

            this.baseMapper.updateById(update);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            MemberEntity regist = new MemberEntity();
            HashMap<String, String> query = new HashMap<>();
            query.put("access_token", socialUser.getAccess_token());
            query.put("uid", socialUser.getUid());
            try {
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                //System.out.println(EntityUtils.toString(response.getEntity()));
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1: 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.insert(regist);
            return regist;
        }
    }
}