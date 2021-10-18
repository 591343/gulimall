package com.xiaochen.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * Ʒ?
 * 
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-20 19:15:15
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Ʒ??id
	 */
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空")
	private String name;
	/**
	 * 品牌Logo
	 */
	@NotBlank(message = "品牌Logo不能为空")
	@URL(message = "品牌Logo必须为URL")
	private String logo;
	/**
	 * 描述
	 */
	@NotBlank(message = "品牌描述不能为空")
	private String descript;
	/**
	 * 显示[1显示0不显示]
	 */
	@NotNull(message = "显示状态码不能为空")
	@Min(value = 0,message = "最小值必须为0")
	@Max(value = 1,message = "最大值必须为1")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不能为空")
	@Size(min = 1,max = 1,message = "检索首字母长度必须为1")
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须在a-z或A-Z")
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序码不能为空")
	@Min(value = 0,message = "排序码必须大于等于0")
	private Integer sort;

}
