package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //查询token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if(StringUtils.isBlank(token)){
            //未登录，返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        //有token，查询用户信息
        try {
            //解析成功，证明已经登录
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            THREAD_LOCAL.set(userInfo);
            return true;
        }catch (Exception e){
            // 抛出异常，证明未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }
}
