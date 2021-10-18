package com.xiaochen.gulimall.auth.controller;


import com.alibaba.fastjson.TypeReference;
import com.xiaochen.common.constant.AuthServerConstant;
import com.xiaochen.common.exception.BizCodeEnum;
import com.xiaochen.common.to.MemberInfoTo;
import com.xiaochen.common.utils.R;
import com.xiaochen.gulimall.auth.feign.MemberFeignService;
import com.xiaochen.gulimall.auth.feign.ThirdPartyFeignService;
import com.xiaochen.gulimall.auth.vo.UserLoginUpVo;
import com.xiaochen.gulimall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    
    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 短信验证服务
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //1.接口防刷,防止在倒计时内从新调用接口,从而形成恶意攻击
        //2.验证码再次校验
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            Long formerTime = Long.parseLong(redisCode.split("_")[1]);

            if((System.currentTimeMillis()-formerTime)<60*1000){
                return R.error(BizCodeEnum.SMS_VAILD_EXCEPTION.getCode(),BizCodeEnum.SMS_VAILD_EXCEPTION.getMessage());
            }else {
                //TODO 60s到期;
            }
        }
        UUID uuid = UUID.randomUUID();
        String sixCode = uuid.toString().substring(0, 6);
        String code = uuid.toString().substring(0, 6)+"_"+System.currentTimeMillis();
        R r = thirdPartyFeignService.sendCode(phone, sixCode);
        //10分钟有效
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,20L, TimeUnit.MINUTES);
        return r;
    }

    /**
     *
     * @param registerVo
     * @param result
     * @param redirectAttributes 重定向携带数据
     * @return
     */
    @PostMapping("/register")
    public String signUp(@Valid UserRegisterVo registerVo, BindingResult result, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){

            Map<String,String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        String code = registerVo.getCode();
        String phone = registerVo.getPhone();
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(redisCode==null){
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        String verifiedCode = redisCode.split("_")[0];
        if(code.equals(verifiedCode)){  //验证码相等,远程调用member服务
            //令牌机制
            stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
            R r = memberFeignService.signUp(registerVo);

            if(r.getCode()!=0){
                Map<String,String> errors = new HashMap<>();
                errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else { //验证码不相等
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //注册
        System.out.println("jinlaile");
        return "redirect:http://auth.gulimall.com/login.html";
    }


    @PostMapping("/login")
    public String loginIn(UserLoginUpVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeignService.signIn(vo);
        if(r.getCode()!=0){ //登陆失败
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
        session.setAttribute(AuthServerConstant.LOGIN_USER,r.getData("data",new TypeReference<MemberInfoTo>(){}));
        return "redirect:http://gulimall.com";
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute!=null){
            return "redirect:http://gulimall.com";
        }else{
            return "login";
        }
    }

}
