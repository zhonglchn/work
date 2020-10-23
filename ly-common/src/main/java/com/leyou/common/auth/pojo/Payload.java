package com.leyou.common.auth.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 载荷dto对象
 * @param <T> 用户信息
 */
@Data
public class Payload<T> {
    private String id;
    private T userInfo;
    private Date expiration;
}