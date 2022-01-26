package com.study.gulimall.auth.Vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6,max = 18,message = "用户名必须是6-18为字符")
    private String userName;
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 18,message = "密码必须是6-18为字符")
    private String password;
    @NotEmpty(message = "手机号必须提交")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式错误")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;

}
