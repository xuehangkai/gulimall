package com.study.gulimall.member.Vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {


    private String userName;

    private String password;

    private String phone;


}
