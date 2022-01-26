package com.study.gulimall.ware.vo;

import com.study.common.vo.MemberEntityVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {

    private MemberReceiveAddressVo address;
    private BigDecimal fare;
}
