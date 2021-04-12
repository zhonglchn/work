package com.leyou.sms.listener;

import com.leyou.common.constant.MQConstants;
import com.leyou.sms.utils.SmsHelper;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhongliang
 * @date 2020/10/20 17:06
 */
@Component
@Slf4j
public class SmsListener {

    @Autowired
    private SmsHelper smsHelper;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.SMS_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.VERIFY_CODE_KEY
    ))
    public void sendCheckCodeListener(Map<String,String> msgMap){
        if(CollectionUtils.isEmpty(msgMap)){
            log.error("【短信监听异常】接收到的数据是空！");
            return;
        }

        String phone = msgMap.get("phone");
        String code = msgMap.get("code");

        //发送消息
        smsHelper.sendCheckCodeMsg(phone,code);

    }
}
