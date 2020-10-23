package com.leyou.user.client;

import com.leyou.user.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

    /**
     * 根据用户名和密码查询用户
     */
    @GetMapping("/query")
    User findUserByUsernameAndPassword(@RequestParam("username") String username,
                                       @RequestParam("password") String password);
        
}