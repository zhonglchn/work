package com.leyou.page.listener;

import com.leyou.common.constant.MQConstants;
import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author zhongliang
 * @date 2020/10/19 23:55
 */
@Component
public class MqListener {

    @Autowired
    private PageService pageService;

    /**
     * 消费监听消息队列  添加静态页
     * @param id  必须和发送方的参数类型保持一致
     */
    @RabbitListener(bindings =@QueueBinding(
            value = @Queue(value = MQConstants.Queue.PAGE_ITEM_UP,durable = "true"),//指定当前监听的队列
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void addStaticPage(Long id){
        pageService.createItemStaticPage(id);
    }



    /**
     * 消费监听消息队列 删除静态页
     * @param id  必须和发送方的参数类型保持一致
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.PAGE_ITEM_DOWN,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void delStaticPage(Long id){
        pageService.delStaticPage(id);
    }
}
