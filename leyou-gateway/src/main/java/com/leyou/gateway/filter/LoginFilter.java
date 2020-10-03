package com.leyou.gateway.filter;


import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        List<String> allowPaths = filterProperties.getAllowPaths();
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        String uri = request.getRequestURI();
        //判断白名单
        //遍历允许访问的路径
        for (String allowPath : allowPaths) {
            if(uri.startsWith(allowPath)){
                return false;
            }
        }
        return true;
    }

    @Override
    public Object run() {

        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            // 校验通过什么都不做，即放行
            JwtUtils.getInfoFromToken(token,jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            // 校验出现异常，返回403
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
            LOGGER.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }
        return null;
    }
}
