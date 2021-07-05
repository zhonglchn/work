package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<Long> saveOrder(@RequestBody OrderDTO orderDTO){
        Long orderId = orderService.saveOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
}