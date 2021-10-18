package com.xiaochen.gulimall.order.feign;


import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/{memberid}/addresses")
    List<MemberAddressVo> memberAddress(@PathVariable("memberid") Long memberId);

}
