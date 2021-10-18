package com.xiaochen.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * ??Ա?ջ???ַ
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:12:50
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);


    List<MemberReceiveAddressEntity> listMemberAddress(Long memberId);
}

