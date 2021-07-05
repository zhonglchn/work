package com.leyou.order.service;

import com.leyou.common.auth.pojo.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.entity.Sku;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.dto.OrderStatusEnum;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.AddressDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderLogisticsMapper logisticsMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private IdWorker idWorker;

    public Long saveOrder(OrderDTO orderDTO) {
        //获取订单id
        Long orderId = idWorker.nextId();
        //创建订单
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(OrderStatusEnum.INIT.value());
        order.setUserId(UserHolder.getUserId());
        order.setPaymentType(1);
        order.setInvoiceType(0);
        order.setPostFee(0L);//邮费包邮

        //得到购买的购物车信息
        List<CartDTO> carts = orderDTO.getCarts();
        //把list结构的购物车对象变成map
        Map<Long, Integer> cartsMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //通过购物车信息得到skuId的集合
        List<Long> skuIds = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //得到所有的sku对象
        List<Sku> skus = itemClient.findSkusByIds(skuIds);
        //遍历skus的集合并计算出总金额 计算方式是 每个sku的价格乘以购买数量，最终一起相加
        long totalFee = skus.stream().mapToLong(sku -> sku.getPrice() * cartsMap.get(sku.getId())).sum();
        order.setTotalFee(totalFee);//总金额，所有sku金额相加
        order.setActualFee(1L);//实际付款金额 写一分钱，便于测试
        //保存订单
        orderMapper.insertSelective(order);

        //定义一个订单详情列表对象
        List<OrderDetail> detailList = new ArrayList<>();
        //遍历sku对象的集合
        skus.forEach(sku -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(idWorker.nextId());
            orderDetail.setOrderId(orderId);
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setNum(cartsMap.get(sku.getId()));
            orderDetail.setCreateTime(new Date());
            orderDetail.setUpdateTime(new Date());
            detailList.add(orderDetail);
        });
        //保存订单详情
        detailMapper.insertList(detailList);
        //查询用户物流信息
        AddressDTO addressDTO = userClient.queryAddressById(UserHolder.getUserId(), orderDTO.getAddressId());
        //把dto对象转成entity对象
        OrderLogistics orderLogistics = BeanHelper.copyProperties(addressDTO, OrderLogistics.class);
        orderLogistics.setOrderId(orderId);
        //保存订单物流数据
        logisticsMapper.insertSelective(orderLogistics);
        //减库存 一定要加到最后
        itemClient.minusStock(cartsMap);
        return orderId;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
