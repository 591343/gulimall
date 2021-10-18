package com.xiaochen.gulimall.order.feign;


import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.order.vo.request.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("gulimall-ware")
@RequestMapping("/ware/waresku")
public interface WmsFeignService {
    /**
     * 检查skus是否有库存
     * @param skuIds
     * @return
     */
    @PostMapping("/hasstock")
    //@RequiresPermissions("ware:waresku:list")
    R hasStock(@RequestBody List<Long> skuIds);

    /**
     * 计算运费
     * @param attrId
     * @return
     */
    @GetMapping("/fare")
    R getFare(@RequestParam("addrId") Long attrId);

    /**
     * 对订单中的商品进行锁库存
     * @param vo
     * @return
     */
    @PostMapping("/lock/order")
    R lockOrderSku(@RequestBody WareSkuLockVo vo);
}
