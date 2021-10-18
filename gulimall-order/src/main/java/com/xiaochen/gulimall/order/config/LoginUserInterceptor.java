package com.xiaochen.gulimall.order.config;


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
        // 这个请求直接放行
        boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
        if(match){
            return true;
        }

        HttpSession session = request.getSession();
        MemberInfoTo memberInfoTo = (MemberInfoTo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberInfoTo!=null){
            threadlocal.set(memberInfoTo);
            return true;
        }else {
            //没登陆就去登陆
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
