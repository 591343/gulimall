package com.xiaochen.gulimall.auth.feign;

import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.auth.vo.UserLoginUpVo;
import com.xiaochen.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R signUp(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R signIn(@RequestBody UserLoginUpVo vo);
}
