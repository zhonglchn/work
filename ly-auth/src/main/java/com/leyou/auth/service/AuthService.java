package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
     * 创建token并写入到浏览器
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

    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        //从Cookie中获取token
        String token = CookieUtils.getCookieValue(request, jwtProp.getUser().getCookieName());
        Payload<UserInfo> payload = null;
        // 解析token
        try {
            payload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), UserInfo.class);
        } catch (Exception e) {
            //token解析报错则表明当前用户没有登录或者登录已过期
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //获取当前tokenID
        String tokenId = payload.getId();
        //检查当前token是否在黑名单
        if (redisTemplate.hasKey(tokenId)) {
            // 如果在黑名单，表示当前用户已经退出登录了
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

        // 获取过期时间
        Date expData = payload.getExpiration();
        // 获取到用户信息
        UserInfo userInfo = payload.getUserInfo();
        //通过过期时间，减去刷新时间点，来得到刷新的具体时间
        DateTime refreshDateTime = new DateTime(expData).minusMinutes(jwtProp.getUser().getRefreshTime());
        //如果当前时间在刷新时间后刷新
        if(refreshDateTime.isBeforeNow()){
            //重新生成token并写入到浏览器
            createTokenToCookie(response, userInfo);
        }
        return userInfo;
    }

    /**
     * 用户退出
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 获取当前token
        String token  = CookieUtils.getCookieValue(request, jwtProp.getUser().getCookieName());
        // token过期时间为30分钟
        Payload<Object> payload = null;
        //解析token
        try {
            payload = JwtUtils.getInfoFromToken(token,jwtProp.getPublicKey());
            String tokenId = payload.getId();
            // 获取过期时间
            Date expiration = payload.getExpiration();
            // 得到过期时间距离现在的毫秒数
            long remainTime = expiration.getTime() - System.currentTimeMillis();
            // 假如不到3秒就过期，就不再存了，直接删除cookie
            if(remainTime>3000){
                // 存入黑名单
                redisTemplate.opsForValue().set(tokenId,"随便写",remainTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            //如果失败，不用管了
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        // 删除浏览器cookie
        CookieUtils.deleteCookie(jwtProp.getUser().getCookieName(),jwtProp.getUser().getCookieDomain(),response);
    }
}