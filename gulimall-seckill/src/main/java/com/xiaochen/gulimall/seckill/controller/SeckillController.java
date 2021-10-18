package com.xiaochen.gulimall.seckill.controller;

import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.seckill.service.SeckillService;
import com.xiaochen.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {

	@Autowired
	private SeckillService seckillService;

	@ResponseBody
	@GetMapping("/currentSeckillSkus")
	public R getCurrentSeckillSkus(){
		List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
		return R.ok().setData(vos);
	}

	/**
	 * 查询商品秒杀信息
	 * @param skuId
	 * @return
	 */
	@ResponseBody
	@GetMapping("/sku/seckill/{skuId}")
	public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
		SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
		return R.ok().setData(to);
	}

	/**
	 *
	 * @param killId
	 * @param key random code
	 * @param num product num
	 * @param model
	 * @return
	 */
	@GetMapping("/kill")
	public String secKill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num, Model model){
		// if seckill is successful, returning the a orderSn
		String orderSn = seckillService.kill(killId,key,num);
		// 1.判断是否登录
		model.addAttribute("orderSn", orderSn);
		return "success";
	}
}