package com.xiaochen.gulimall.ware.feign;

import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.ware.vo.respone.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    /**
     * 获取用户选择的地址
     */
    @RequestMapping("/member/memberreceiveaddress/addr/{id}")
    //@RequiresPermissions("member:memberreceiveaddress:info")
    MemberAddressVo addr(@PathVariable("id") Long id);

}
