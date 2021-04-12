package com.leyou;

import com.leyou.com.leyou.gateway.config.FilterProperties;
import com.leyou.com.leyou.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author zhongliang
 * @date 2020/9/17 19:36
 */
@SpringCloudApplication
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyGatewayApplication.class,args);
    }
}
