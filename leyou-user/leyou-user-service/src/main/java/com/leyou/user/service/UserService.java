package com.leyou.user.service;

import com.leyou.user.pojo.User;

public interface UserService {

    Boolean checkUserData(String data, Integer type);

    Boolean sendVerifyCode(String phone);

    Boolean register(User user, String code);

    User queryUser(String username, String password);
}
