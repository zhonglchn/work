package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProp;


    public void login(String username, String password, HttpServletResponse response) {

        try {
            // 查询出用户
            User user = userClient.findUserByUsernameAndPassword(username, password);
            // 转成jwt载荷中的userInfo对象
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), "admin");

            // 生成token
            createTokenToCookie(response, userInfo);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    /**
     * 创建token并写入到浏览器o
     */
    private void createTokenToCookie(HttpServletResponse response, UserInfo userInfo) {
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProp.getPrivateKey(), jwtProp.getUser().getExpire());
        // 写入到浏览器中
        CookieUtils.newCookieBuilder()
                .name(jwtProp.getUser().getCookieName())
                .domain(jwtProp.getUser().getCookieDomain())
                .response(response)
                .value(token)
                .httpOnly(true)
                .build();
    }
}