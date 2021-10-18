package com.xiaochen.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xiaochen.common.to.MemberInfoTo;
import com.xiaochen.common.to.mq.SecKillOrderTo;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.seckill.feign.CouponFeignService;
import com.xiaochen.gulimall.seckill.feign.ProductFeignService;
import com.xiaochen.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.xiaochen.gulimall.seckill.service.SeckillService;
import com.xiaochen.gulimall.seckill.to.SeckillSkuRedisTo;
import com.xiaochen.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.xiaochen.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {


	@Autowired
    CouponFeignService couponFeignService;

    @Autowired
	StringRedisTemplate stringRedisTemplate;

    @Autowired
	ProductFeignService productFeignService;

    @Autowired
	RedissonClient redissonClient;

    @Autowired
	LoginUserInterceptor loginUserInterceptor;

    @Autowired
	RabbitTemplate rabbitTemplate;

    private final String SESSION_CACHE_PREFIX="seckill:sessions:";

	private final String SKUKILL_CACHE_PREFIX="seckill:skus:";
	private static final String SKUSTOCK_SEMAPHONE = "seckill:stock:"; // +商品随机码
	private final Integer LENGTH_ORDER_NUMBER=12;
	private final String EXCHANGE="order-event-exchange";
	private final String ROUTING_KEY="order.seckill.order";




	@Override
	public void uploadSeckillSkuLatest3Day() {
		// 1.扫描最近三天要参加秒杀的商品
		R r = couponFeignService.getLatest3DaySession();
		if(r.getCode() == 0){
			List<SeckillSessionsWithSkus> sessions = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {});
			for (SeckillSessionsWithSkus session : sessions) {
				System.out.println(session);
			}
			// 2.缓存活动信息
			saveSessionInfo(sessions);
			// 3.缓存活动的关联的商品信息
			saveSessionSkuInfo(sessions);
		}
	}


	@Override
	public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

//		// 1.确定当前时间属于那个秒杀场次
//		long time = new Date().getTime();
//		// 定义一段受保护的资源
//		try (Entry entry = SphU.entry("seckillSkus")){
//			//获取匹配该模式的所有键
//			Set<String> keys = stringRedisTemplate.keys(SESSION_CACHE_PREFIX + "*");
//			for (String key : keys) {
//				// seckill:sessions:1593993600000_1593995400000
//				String replace = key.replace("seckill:sessions:", "");
//				String[] split = replace.split("_");
//				long start = Long.parseLong(split[0]);
//				long end = Long.parseLong(split[1]);
//				if(time >= start && time <= end){
//					// 2.获取这个秒杀场次的所有商品信息
//					List<String> range = stringRedisTemplate.opsForList().range(key, 0, 100);
//					BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
//					List<String> list = hashOps.multiGet(range);
//					if(list != null){
//						return list.stream().map(item -> {
//							SeckillSkuRedisTo redisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
////						redisTo.setRandomCode(null);
//							return redisTo;
//						}).collect(Collectors.toList());
//					}
//					break;
//				}
//			}
//		}catch (BlockException e){
//			log.warn("资源被限流：" + e.getMessage());
//		}
		return null;
	}
//
	@Override
	public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
		BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
		Set<String> keys = hashOps.keys();
		if(keys != null && keys.size() > 0){
			String regx = "\\d-" + skuId;
			for (String key : keys) {
				if(Pattern.matches(regx, key)){
					String json = hashOps.get(key);
					SeckillSkuRedisTo to = JSON.parseObject(json, SeckillSkuRedisTo.class);
					// 处理一下随机码
					long current = new Date().getTime();

					if(current <= to.getStartTime() || current >= to.getEndTime()){
						to.setRandomCode(null);
					}
					System.out.println("秒杀信息"+to);
					return to;
				}
			}
		}
		return null;
	}

	@Override
	public String kill(String killId, String key, Integer num) {

		BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
		String s = ops.get(killId);
		ThreadLocal<MemberInfoTo> threadlocal = loginUserInterceptor.threadlocal;

		if(!StringUtils.isEmpty(s)){
			SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
			Long startTime = seckillSkuRedisTo.getStartTime();
			Long endTime = seckillSkuRedisTo.getEndTime();
			long localTime = new Date().getTime();

			if(localTime>=startTime&&localTime<=endTime){
				String killSkuId =  seckillSkuRedisTo.getPromotionSessionId()+"-"+seckillSkuRedisTo.getSkuId();
				String randomCode =seckillSkuRedisTo.getRandomCode();
				if(randomCode.equals(key)&&killId.equals(killSkuId)){
					int limitCount = seckillSkuRedisTo.getSeckillLimit().intValue();
						if(num<=limitCount){
							String username = threadlocal.get().getUsername();
							String redisKey=username+"_"+ seckillSkuRedisTo.getPromotionSessionId()+"_"+seckillSkuRedisTo.getSkuId();
							// setIfAbsent is a atomic operation
							long ttl = endTime - startTime;
							Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey,num.toString(),ttl, TimeUnit.MILLISECONDS);
							//到Redis进行占位，如果成功证明从来没有买过,否则买过了，不能再买
							if(aBoolean){
								RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + key);
								try {
									//非阻塞方法，能拿到信号量就拿，拿不到就走了
									boolean isSuccess = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
									//向MQ发送消息进行订单处理
									if(isSuccess){
										String orderSn = IdWorker.getTimeId().substring(0,LENGTH_ORDER_NUMBER);
										SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
										secKillOrderTo.setOrderSn(orderSn);
										secKillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
										secKillOrderTo.setNum(secKillOrderTo.getNum());
										secKillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
										secKillOrderTo.setMemberId(threadlocal.get().getId());
										secKillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
										rabbitTemplate.convertAndSend(EXCHANGE,ROUTING_KEY,secKillOrderTo);
										return orderSn;
									}
								} catch (InterruptedException e) {
									log.error("用户："+username+"秒杀失败");
								}
							}
						}
				}
			}
		}
		return null;
	}

	private void saveSessionInfo(List<SeckillSessionsWithSkus> sessions){
		if(sessions != null){
			sessions.stream().forEach(session -> {
				long startTime = session.getStartTime().getTime();

				long endTime = session.getEndTime().getTime();
				String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
				Boolean hasKey = stringRedisTemplate.hasKey(key);
				if(!hasKey){
					// 获取所有商品id
					List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "-" + item.getSkuId()).collect(Collectors.toList());
					// 缓存活动信息
					for (String s : collect) {
						System.out.print(s+",");
					}
					System.out.println();
					stringRedisTemplate.opsForList().leftPushAll(key, collect);
				}
			});
		}
	}
//
	private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessions){
		if(sessions != null){
			sessions.stream().forEach(session -> {
				BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
				session.getRelationSkus().stream().forEach(seckillSkuVo -> {
					// 1.商品的随机码,防止恶意请求攻击
					String randomCode = UUID.randomUUID().toString().replace("-", "");
					//添加seckillSkuVo.getPromotionSessionId()场次使得不同场次的相同skuID都能上架
					if(!ops.hasKey(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId())){
						// 2.缓存商品
						SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
						BeanUtils.copyProperties(seckillSkuVo, redisTo);
						// 3.sku的基本数据 sku的秒杀信息
						R info = productFeignService.info(seckillSkuVo.getSkuId());
						if(info.getCode() == 0){
							SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
							redisTo.setSkuInfoVo(skuInfo);
						}
						// 4.设置当前商品的秒杀信息
						redisTo.setStartTime(session.getStartTime().getTime());
						redisTo.setEndTime(session.getEndTime().getTime());

						redisTo.setRandomCode(randomCode);

						ops.put(seckillSkuVo.getPromotionSessionId() + "-" + seckillSkuVo.getSkuId(), JSON.toJSONString(redisTo));
						// 如果当前这个场次的商品库存已经上架就不需要上架了
						// 5.使用库存作为分布式信号量  限流(扣减库存必须带随机码)
						RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHONE + randomCode);
						//使用库存数量作为信号量
						semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
					}
				});
			});
		}
	}
}