package com.example.product_test.order.service;

import com.example.product_test.order.model.Order;

public interface SeckillOrderService {
    String submitSeckill(Long userId, Long productId);

    void payOrder(String orderNo);

    void cancelOrder(String orderNo);

    Order getByOrderNo(String orderNo);

    Order getByUserAndProduct(Long userId, Long productId);
}
