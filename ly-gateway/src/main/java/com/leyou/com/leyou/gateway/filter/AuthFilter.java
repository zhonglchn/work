package com.leyou.com.leyou.gateway.filter;

import com.leyou.com.leyou.gateway.config.FilterProperties;
import com.leyou.com.leyou.gateway.config.JwtProperties;
import com.leyou.common.auth.pojo.Payload;
import com.leyou.common.auth.pojo.UserInfo;
import com.leyou.common.constant.LyConstant;
import com.leyou.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author zhongliang
 * @date 2020/10/24 20:03
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;

    /**
     * @param exchange 网关过滤器api对象
     * @param chain    过滤器链
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // gateway封装的request域
        ServerHttpRequest request = exchange.getRequest();

        //判断如果当前请求在白名单中，直接放行
        if (isAllowRequest(request)) {
            //继续后续的操作
            return chain.filter(exchange);
        }

        // 获取请求的cookie
        HttpCookie cookie = request.getCookies().getFirst(jwtProp.getUser().getCookieName());

        // 判断cookie是否有值
        if (cookie == null) {
            //当前用户没有登录，终止后续所有操作，其他过滤链包括要转发的微服务都不再访问,401没登录，403是没权限
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 获取token
        String token = cookie.getValue();

        Payload<UserInfo> payload = null;
        try {
            //解析token
            payload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), UserInfo.class);

            // 解析没有问题，证明已经登录
            UserInfo user = payload.getUserInfo();
            // TODO: 做权限校验
            String role = user.getRole();
            System.out.println("角色======>" + role);
            String path = request.getURI().getPath();
            System.out.println("路径======> " + path);
            String method = request.getMethodValue();
            System.out.println("方法类型：=====>" + method);
            // TODO 判断权限，此处暂时空置，等待权限服务完成后补充
            log.info("【网关】用户{},角色{}。访问服务{} : {}，", user.getUsername(), role, method, path);
            // 说明：只要是非白名单请求，一定需要当前用户id，所以这里我们可以得到当前用户id，并放入请求头中传下去
            // //获取用户id
             Long userId = user.getId();
            //给当前request请求添加请求头，并得到一个新的request对象
            ServerHttpRequest newRequest = exchange.getRequest().mutate().header(LyConstant.USER_ID_HEADER, userId.toString()).build();
            //修改exchange对象中的request对象，并得到一个新的exchange
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
            return chain.filter(newExchange);
        } catch (Exception e) {
            //当前用户没有登录，终止后续所有操作，其他过滤链包括要转发的微服务都不再访问,401没登录，403是没权限
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isAllowRequest(ServerHttpRequest request) {
        //得到请求路径，这里是完整的url地址除去域名部分 比如：http://www.leyou.com/api/item/xxx
        //这里得到的就是/api/item/xxx
        String path = request.getURI().getPath();
        // 获取白名单列表
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (path.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当前过滤器在过滤器链中的执行位置，一般自定义的都写0，越小越靠前
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
