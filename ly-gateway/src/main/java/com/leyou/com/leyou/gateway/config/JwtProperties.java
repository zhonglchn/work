package com.leyou.com.leyou.gateway.config;

import com.leyou.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @author zhongliang
 * @date 2020/10/24 20:00
 */
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {

    private String pubKeyPath;

    private PublicKey publicKey;

    private JwtUser user = new JwtUser();

    @Data
    public class JwtUser{
        private String cookieName;
    }

    //当对象彻底创建成功之后自动执行
    @PostConstruct
    public void initMethod() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
