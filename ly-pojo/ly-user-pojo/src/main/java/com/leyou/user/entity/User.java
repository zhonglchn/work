package com.leyou.user.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Size(min = 4,max = 16,message = "用户名个数不正确")
    private String username;
    @Length(min = 6,max = 16,message = "密码格式不正确")
    private String password;
    private String phone;
    private Date createTime;
    private Date updateTime;
}