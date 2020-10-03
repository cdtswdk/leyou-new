package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.impl.AuthServiceImpl;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        //登录验证
        String token = this.authService.authentication(username,password);
        if(StringUtils.isBlank(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),
                token,jwtProperties.getExpire()*60,null,true);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 验证用户信息
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN")String token,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        try {
            // 从token中解析token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            if(userInfo == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            //重新刷新token
            token = JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());

            //重新刷新cookie
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60);
            // 解析成功返回用户信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 出现异常则，响应500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
