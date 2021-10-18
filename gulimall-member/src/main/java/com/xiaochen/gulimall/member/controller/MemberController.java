package com.xiaochen.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.xiaochen.common.exception.BizCodeEnum;
import com.xiaochen.gulimall.member.exception.ExistPhoneNumberException;
import com.xiaochen.gulimall.member.exception.ExistUserNameException;
import com.xiaochen.gulimall.member.feign.CouponService;
import com.xiaochen.gulimall.member.vo.MemberRegisterVo;
import com.xiaochen.gulimall.member.vo.UserLoginUpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.xiaochen.gulimall.member.entity.MemberEntity;
import com.xiaochen.gulimall.member.service.MemberService;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.R;



/**
 * ??Ա
 *
 * @author chenxiao
 * @email 591343671@qq.com
 * @date 2021-07-21 14:12:50
 */
@RefreshScope
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponService couponService;
    @Value("${member.user.name}")
    private String name;
    @Value("${member.user.age}")
    private Integer age;

    @RequestMapping("/config")
    public R config(){
        return R.ok().put("name",name).put("age",age);
    }

    @RequestMapping("/coupons")
    public R coupons(){
        MemberEntity memberEntity=new MemberEntity();
        memberEntity.setNickname("张三");
        R r=couponService.memberCoupons();

        return R.ok().put("member",memberEntity).put("coupons",r.get("coupons"));
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/register")
    public R signUp(@RequestBody MemberRegisterVo vo){
        try {
            memberService.signUpMember(vo);
        }catch (ExistUserNameException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }catch (ExistPhoneNumberException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R signIn(@RequestBody UserLoginUpVo vo){
        MemberEntity memberEntity=memberService.login(vo);
        if(memberEntity!=null){
            return R.ok().put("data",memberEntity);
        }
        return R.error(BizCodeEnum.Login_FAILD_EXCEPTION.getCode(),BizCodeEnum.Login_FAILD_EXCEPTION.getMessage());
    }




}
