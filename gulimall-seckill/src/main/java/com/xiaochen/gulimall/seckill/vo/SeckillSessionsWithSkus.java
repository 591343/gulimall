package com.xiaochen.gulimall.seckill.vo;

import lombok.Data;
import lombok.ToString;

import java.sql.Date;
import java.util.List;

/**
 * 秒杀场次skus
 */
@Data
@ToString
public class SeckillSessionsWithSkus {

	private Long id;
	/**
	 * 场次名称
	 */
	private String name;
	/**
	 * 每日开始时间
	 */
	private Date startTime;
	/**
	 * 每日结束时间
	 */
	private Date endTime;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 该秒杀场次相关联的商品sku
	 */
	private List<SeckillSkuRelationEntity> relationSkus;
}