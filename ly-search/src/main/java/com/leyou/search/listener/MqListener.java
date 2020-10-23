package com.leyou.search.listener;

import com.leyou.common.constant.MQConstants;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhongliang
 * @date 2020/10/20 13:56
 */
@Component
public class MqListener {

    @Autowired
    private SearchService searchService;

    /**
     * 如果监听到上架操作，则添加索引库
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SEARCH_ITEM_UP,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void addIndex(Long spuId){
        searchService.addIndex(spuId);
    }



    /**
     * 如果监听到下架操作，则删除索引库
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SEARCH_ITEM_DOWN, durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void delIndex(Long spuId){
        searchService.delIndex(spuId);
    }
}
