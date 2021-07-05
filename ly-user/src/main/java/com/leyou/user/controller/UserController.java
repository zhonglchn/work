package com.leyou.user.controller;

import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import org.hamcrest.core.Is;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * @author zhongliang
 * @date 2020/10/23 13:43
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUsernameOrPhone(@PathVariable("data") String data,
                                                        @PathVariable("type") Integer type) {

        Boolean isTrue = userService.checkUsernameOrPhone(data, type);
        return ResponseEntity.ok(isTrue);
    }

    @PostMapping("/code")
    public ResponseEntity<Void> sendCheckCode(@RequestParam("phone") String phone){
        userService.sendCheckCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user 用户信息
     * @param code 验证码
     * @param result BindingResult这个对象要紧挨着被验证的对象放，可以起到收集数据格式异常的作用
     */
    @PostMapping("/register")
    public ResponseEntity<Void> userRegister(@Valid User user, BindingResult result,
                                             @RequestParam("code") String code){
        // 判断是否有数据格式异常
        if(result.hasErrors()){
            String errorMsg = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            throw new LyException(501,errorMsg);
        }
        userService.userRegister(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     *
     * 测试dev01
     */
    @GetMapping("/query")
    public ResponseEntity<User> findUserByUsernameAndPassword(@RequestParam("username") String username,

                                                              @RequestParam("password") String password){
        User user = userService.findUserByUsernameAndPassword(username, password);
        return ResponseEntity.ok(user);
    }
}
