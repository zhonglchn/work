package com.leyou.user.service;

import com.leyou.common.constant.LyConstant;
import com.leyou.common.constant.MQConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.user.config.PasswordConfig;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhongliang
 * @date 2020/10/23 13:43
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public Boolean checkUsernameOrPhone(String data, Integer type) {

        User user = new User();
        switch (type){
            case 1:
                // type为1则按照用户名查询
                user.setUsername(data);
                break;
            case 2:
                // type为2则按照手机号查询
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        // 能在数据库中查到记录就说明用户名或手机号已存在
        int count = userMapper.selectCount(user);

        return count==0;
    }

    /**
     * 检查验证码
     * @param phone 手机号
     */
    public void sendCheckCode(String phone) {
        // redisKey前缀
        String key = LyConstant.CHECK_CODE_PRE+phone;
        // 生成6位数字短信验证码
        String code = RandomStringUtils.randomNumeric(6);

        // 存入redis中，过期时间2小时
        redisTemplate.opsForValue().set(key,code,2,TimeUnit.HOURS);

        // 消息队列需要的信息
        Map<String, String> map = new HashMap<>();
        map.put("phone",phone);
        map.put("code",code);

        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                map);

    }

    /**
     * 用户注册
     * @param user 用户信息
     * @param code 验证码
     */
    public void userRegister(User user, String code) {
        // 获取redis中的验证码
        String redisCode = redisTemplate.opsForValue().get(LyConstant.CHECK_CODE_PRE + user.getPhone());
        // 查看用户是否输入正确
        if(!StringUtils.equals(code,redisCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        // 给密码进行动态加盐加密
        String encode = encoder.encode(user.getPassword());
        user.setPassword(encode);

        // user对象中没有对创建时间，新增时间进行赋值，所以用下列方法，选择有值的进行插入，没值的数据库会自动生成
        int count = userMapper.insertSelective(user);
        if(count==0){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
}
