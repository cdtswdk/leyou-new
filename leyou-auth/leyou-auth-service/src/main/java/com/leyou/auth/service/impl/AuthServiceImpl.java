package com.leyou.auth.service.impl;

import com.leyou.auth.client.AuthClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public String authentication(String username, String password) {

        try {
            //调用微服务，查询用户
            User user = this.authClient.queryUser(username, password);
            //如果查询结果为null，直接返回null
            if(user == null){
                return null;
            }
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            //生成token
            return JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
