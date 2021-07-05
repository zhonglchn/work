package com.leyou.cart.interceptor;

import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.constant.LyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhongliang
 * @date 2021/5/17 14:46
 */
@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {
    // 在访问任何处理器之前执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取到当前用户的id
        String userIdStr = request.getHeader(LyConstant.USER_ID_HEADER);
        UserHolder.setUserId(Long.valueOf(userIdStr));
        log.info("【购物车微服务】从网关的请求头中获取到了当前的用户id为："+userIdStr);
        return true;
    }

    // 当前处理器彻底执行完毕（包括页面渲染）后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.reMoveUserId();
    }
}
