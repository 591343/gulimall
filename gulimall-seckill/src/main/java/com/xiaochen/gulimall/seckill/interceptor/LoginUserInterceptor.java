package com.xiaochen.gulimall.seckill.interceptor;


import com.xiaochen.common.constant.AuthServerConstant;
import com.xiaochen.common.to.MemberInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static  ThreadLocal<MemberInfoTo> threadlocal=new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        // if the seckill service,we need to interceptor
        boolean match = new AntPathMatcher().match("/kill", uri);
        if(match){
            HttpSession session = request.getSession();
            MemberInfoTo memberInfoTo = (MemberInfoTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if(memberInfoTo!=null){
                threadlocal.set(memberInfoTo);
                return true;
            }else {
                //没登陆就去登陆
                request.getSession().setAttribute("msg","请先登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }

        return true;
    }
}
