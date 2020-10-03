package com.leyou.user.service.impl;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String KEY_PREFIX="user:code:phone:";
    @Override
    public Boolean checkUserData(String data, Integer type) {
        User record = new User();
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(record) == 0;
    }

    @Override
    public Boolean sendVerifyCode(String phone) {

        //生成验证码
        String code = NumberUtils.generateCode(6);
        try {
            //发送短信
            Map<String,String> msg = new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",code);
            this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE","sms.verify.code",msg);
            //保存到redis
            this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,25, TimeUnit.MINUTES);
            return true;
        }catch (Exception e){
            LOGGER.error("短信发送失败。phone：{}，code：{}",phone,code);
            return false;
        }
    }

    @Override
    public Boolean register(User user, String code) {

        //校验短信验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if(!StringUtils.equals(code,redisCode)){
            return false;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码加密
        String md5Password = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(md5Password);
        // 强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());
        // 添加到数据库
        boolean b = this.userMapper.insertSelective(user) == 1;
        if(b){
            this.redisTemplate.delete(KEY_PREFIX+user.getPhone());
        }
        return b;
    }

    @Override
    public User queryUser(String username, String password) {
        //查询用户
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        //校验用户名
        if(user == null){
            return null;
        }
        //密码加密
        String salt = user.getSalt();
        String md5Password = CodecUtils.md5Hex(password, salt);
        //校验密码
        if(!StringUtils.equals(md5Password,user.getPassword())){
            return null;
        }
        //用户名和密码都正确
        return user;
    }
}
