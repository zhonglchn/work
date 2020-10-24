package com.leyou.auth.config;

import com.leyou.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;
    private String priKeyPath;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private JwtUser user = new JwtUser();

    @Data
    public class JwtUser{
        private Integer expire;
        private Integer refreshTime;
        private String cookieName;
        private String cookieDomain;
    }

    //当对象彻底创建成功之后自动执行
    @PostConstruct
    public void initMethod() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

}