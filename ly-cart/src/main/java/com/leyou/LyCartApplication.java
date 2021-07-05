package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zhongliang
 * @date 2021/5/17 13:45
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LyCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyCartApplication.class,args);
    }
}
