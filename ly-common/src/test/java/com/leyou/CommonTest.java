package com.leyou;

import com.leyou.common.utils.RsaUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * @author zhongliang
 * @date 2020/10/23 21:25
 */
public class CommonTest {

    private String privatePath = "E:\\Projects\\leyou_work_develop\\rsa\\rsa_key";
    private String publicPath = "E:\\Projects\\leyou_work_develop\\rsa\\rsa_key.pub";

    @Test
    public void createKey() throws Exception {
        /**
         * 注意：第三个参数是生成密钥的时候放置的自定义盐，随便写。
         * 生成的私钥和公钥可以重复使用，任何用户生成jwt，都可以用这一对密钥。
         */
        RsaUtils.generateKey(publicPath, privatePath, "zhongl", 2048);
    }

    @Test
    public void getKey() throws Exception {
        PublicKey publicKey = RsaUtils.getPublicKey(publicPath);
        System.out.println("公钥:"+publicKey);
        PrivateKey privateKey = RsaUtils.getPrivateKey(privatePath);
        System.out.println("私钥"+privateKey);
    }


    /**
     * 生成token
     */
    @Test
    public void createToken() throws Exception {
        PrivateKey privateKey = RsaUtils.getPrivateKey(privatePath);
        String jws = Jwts.builder()
                .setId("11")//当前token的id
                .signWith(privateKey)//指定加密方式
                .setExpiration(new Date(new Date().getTime()+111111111))
                .claim("role", "admin")//自定义属性
                .compact();
        System.out.println(jws);
    }

    /**
     * 解析token
     */
    @Test
    public void parseToken() throws Exception {
        String jws = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIxMSIsImV4cCI6MTYwMzU3MjAwMywicm9sZSI6ImFkbWluIn0.eK9TQ9qZ42arHW28PErJe2Bs3QJnopxLUaidX5zP8Zp2cKh_49FXigtJymP-y9APlPs8zn-mIL7sZx9Y0F8IaNwbYB-EtVYjFBXskV01H5WTTRAEiE03GJvFTe2TLeonTJtisdnLBMG2eOZz7i3Ahzhio65FzMbZ1SJOLjJ1uOAz8PX6nwFxLVtFFzidt3KOdakJ8dDAFmRexOX0Nwy-WGR0m_hEJVVZrgp6EvpffNCF_SHpe_efvQFX0rQv8xHzsl69YxGsBZSn34Y6hmcflfFLZ7ygKNq4WBR0vjXy4us2mH4lQTkOn37DC9PrtHl19gOWhX05RNciAYVDd6W0tg";
        PublicKey publicKey = RsaUtils.getPublicKey(publicPath);
        Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jws).getBody();
        System.out.println(claims.getId());
        System.out.println(claims.getExpiration());
        System.out.println(claims.get("role"));//获取自定义属性
    }
}
