package com.xiaochen.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.ToString;

/**
 * ??ɱ????
 * 
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:10:07
 */
@Data
@ToString
@TableName("sms_seckill_session")
public class SeckillSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * ???????
	 */
	private String name;
	/**
	 * ÿ?տ?ʼʱ?
	 */
	private Date startTime;
	/**
	 * ÿ?ս???ʱ?
	 */
	private Date endTime;
	/**
	 * ????״̬
	 */
	private Integer status;
	/**
	 * ????ʱ?
	 */
	private Date createTime;

	@TableField(exist = false)
	private List<SeckillSkuRelationEntity> relationSkus;

}
