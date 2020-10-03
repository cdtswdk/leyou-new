package com.leyou.auth.test;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\最新2019黑马java全套\\最新2019java黑马全（idea）\\11-乐优商城\\leyou\\day17-授权中心\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\最新2019黑马java全套\\最新2019java黑马全（idea）\\11-乐优商城\\leyou\\day17-授权中心\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4ODM0MzY5MH0.Wbndh3iIOEuYLLKI1QaTvBHaiuFCjt3Wh6-Ih-5UoQ4V5ppbRx4_LImzl0hbltrZSpFEmT_vC2wXr37CMGiAnvOcamYuwGGBpSFlFaFKcl1nbYNA5t4i5BHlxGVaCp-yCzg3Hbi809Gkae1Wc6lndRYsrHLf_nnj5DmfKtzj1Mw";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}