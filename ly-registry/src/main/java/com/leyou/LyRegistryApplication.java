package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Scope;

/**
 * @author zhongliang
 * @date 2020/9/17 19:26
 */
@SpringBootApplication
@EnableEurekaServer
@RefreshScope
@EnableCaching
public class LyRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyRegistryApplication.class,args);
    }
}
