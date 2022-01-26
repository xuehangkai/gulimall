package com.study.common.exception;

public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    TO_MANY_REQUEST(10003,"请求流量过大"),
    SMS_CODE_EXCEPTION(10002,"验证码获取频率过高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户已存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号码已存在"),
    LOGINACCT_PAEEWORD_INVAILD_EXCEPTION(15003,"用户名或密码错误"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足");

    private int code;
    private String msg;
    BizCodeEnume(int code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public int getCode(){
        return this.code;
    }
    public String getMsg(){
        return this.msg;
    }
}
