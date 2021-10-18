package com.xiaochen.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.member.entity.MemberEntity;
import com.xiaochen.gulimall.member.vo.MemberRegisterVo;
import com.xiaochen.gulimall.member.vo.UserLoginUpVo;

import java.util.Map;

/**
 * ??Ա
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:12:50
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void signUpMember(MemberRegisterVo vo);

    MemberEntity login(UserLoginUpVo vo);
}

