package com.example.product_test.order.controller;

import com.example.product_test.common.ApiResponse;
import com.example.product_test.order.model.Order;
import com.example.product_test.order.service.SeckillOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
public class SeckillOrderController {

    private final SeckillOrderService seckillOrderService;

    public SeckillOrderController(SeckillOrderService seckillOrderService) {
        this.seckillOrderService = seckillOrderService;
    }

    @PostMapping("/order")
    public ApiResponse<Map<String, Object>> submit(@RequestParam Long userId, @RequestParam Long productId) {
        String orderNo = seckillOrderService.submitSeckill(userId, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("status", "PENDING");
        result.put("message", "request accepted, wait async order creation");
        return ApiResponse.success(result);
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<Order> getByOrderNo(@PathVariable String orderNo) {
        Order order = seckillOrderService.getByOrderNo(orderNo);
        if (order == null) {
            return ApiResponse.fail("order not found");
        }
        return ApiResponse.success(order);
    }

    @GetMapping("/orders/user/{userId}")
    public ApiResponse<Order> getByUserAndProduct(@PathVariable Long userId, @RequestParam Long productId) {
        Order order = seckillOrderService.getByUserAndProduct(userId, productId);
        if (order == null) {
            return ApiResponse.fail("order not found");
        }
        return ApiResponse.success(order);
    }

    @PostMapping("/pay")
    public ApiResponse<String> pay(@RequestParam String orderNo) {
        seckillOrderService.payOrder(orderNo);
        return ApiResponse.success("payment request accepted");
    }

    @PostMapping("/cancel")
    public ApiResponse<String> cancel(@RequestParam String orderNo) {
        seckillOrderService.cancelOrder(orderNo);
        return ApiResponse.success("cancel request accepted");
    }
}
