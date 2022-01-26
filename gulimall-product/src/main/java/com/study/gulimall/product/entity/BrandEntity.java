package com.study.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.study.common.valid.AddGroup;
import com.study.common.valid.ListValue;
import com.study.common.valid.UpadateStatusGroup;
import com.study.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author xhk
 * @email 626339972@qq.com
 * @date 2021-07-23 17:02:00
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class,UpadateStatusGroup.class})
	@Null(message = "新增不能指定id",groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */

	@NotBlank(message = "品牌名必须提交",groups = {UpdateGroup.class,AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "logo必须为一个合法url地址",groups = {UpdateGroup.class,AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpadateStatusGroup.class})
	@ListValue(vals = {0,1},groups = {AddGroup.class, UpadateStatusGroup.class,UpdateGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须是一个首字母",groups = {UpdateGroup.class,AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0,message = "排序必须大于等于0",groups = {UpdateGroup.class,AddGroup.class})
	private Integer sort;

}
