package com.leyou.sms.test;

import com.leyou.common.constant.MQConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhongliang
 * @date 2020/10/20 17:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MsgTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendMsg(){
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("phone", "13755065155");
        msgMap.put("code", "8888");
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY, msgMap);
    }
}
