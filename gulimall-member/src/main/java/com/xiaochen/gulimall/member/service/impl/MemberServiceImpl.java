package com.xiaochen.gulimall.member.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.xiaochen.gulimall.member.entity.MemberLevelEntity;
import com.xiaochen.gulimall.member.exception.ExistPhoneNumberException;
import com.xiaochen.gulimall.member.exception.ExistUserNameException;
import com.xiaochen.gulimall.member.service.MemberLevelService;
import com.xiaochen.gulimall.member.vo.MemberRegisterVo;
import com.xiaochen.gulimall.member.vo.UserLoginUpVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaochen.common.utils.PageUtils;
import com.xiaochen.common.utils.Query;

import com.xiaochen.gulimall.member.dao.MemberDao;
import com.xiaochen.gulimall.member.entity.MemberEntity;
import com.xiaochen.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void signUpMember(MemberRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setCreateTime(new Date(new java.util.Date().getTime()));
        Long level = memberLevelService.queryDefaultLevel();
        memberEntity.setLevelId(level);
        checkMobile(vo.getPhone());
        checkUserName(vo.getUserName());
        //密码不能进行明文存储，必须进行加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        //存储MD5盐值加密后的密码
        memberEntity.setPassword(encode);
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public MemberEntity login(UserLoginUpVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if(memberEntity!=null){
            String password1 = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, password1); //密码匹配
            if(matches){ //匹配成功
                return memberEntity;
            }
        }
        //登陆失败
        return null;
    }

    public void checkMobile(String phone) throws ExistPhoneNumberException {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(memberEntity!=null)
            throw new ExistPhoneNumberException();
    }

    public void checkUserName(String userName) throws ExistUserNameException {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(memberEntity!=null)
            throw new ExistUserNameException();
    }

}