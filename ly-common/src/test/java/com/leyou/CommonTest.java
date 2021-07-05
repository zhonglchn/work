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
        String jws = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjozMixcInVzZXJuYW1lXCI6XCJ6aG9uZ2xjaG5cIixcInJvbGVcIjpcImFkbWluXCJ9IiwianRpIjoiTUdGak9ETTFPV010TlRabE9DMDBPREptTFRnM05qUXRaRFppWmpRd1pqYzRPR1k1IiwiZXhwIjoxNjIxMTc5MDY4fQ.TLeebox6N7mV2DgqOz_kASCdH3TsedL8E1aWAjJfgypTax7D-juoNwaBMI4WIc6exhmY7Q5LJIPMLixAYJBg1MK60G3o64AjYw6ox_Zy29EA-cDxvERdwJcnjzliZ5E-Db19j1v7vux6iaTrRHbpJrjdk3IxtPHUDDh_Gs3_Kv0_za6Bi1pDK9uMG0xpSIne3hLFy5VyNahSdSc7Vj4GneG2n3WhhiOwfd6tnqupmlNGSMVoOsB4HUyt9-gBJEJRlZD80T79qTe44q8nVTsLd9QIInOC5pE5sGKLPrlKKRkN6xzNX2W-fvp5nVO5TYf-887pP1J9RVyGkfZBI1B7gA";
        PublicKey publicKey = RsaUtils.getPublicKey(publicPath);
        Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jws).getBody();
        System.out.println(claims.getId());
        System.out.println(claims.getExpiration());
        System.out.println(claims.get("role"));//获取自定义属性
    }
}
