package com.example.product_test.order.service.impl;

import com.example.product_test.order.dto.SeckillOrderMessage;
import com.example.product_test.order.mapper.InventoryMapper;
import com.example.product_test.order.mapper.OrderMapper;
import com.example.product_test.order.model.Order;
import com.example.product_test.order.mq.SeckillOrderProducer;
import com.example.product_test.order.service.SeckillOrderService;
import com.example.product_test.order.util.SnowflakeIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String IDEMPOTENT_KEY = "seckill:once:";

    private final StringRedisTemplate redisTemplate;
    private final InventoryMapper inventoryMapper;
    private final OrderMapper orderMapper;
    private final SeckillOrderProducer producer;
    private final SnowflakeIdGenerator idGenerator;

    public SeckillOrderServiceImpl(StringRedisTemplate redisTemplate,
                                   InventoryMapper inventoryMapper,
                                   OrderMapper orderMapper,
                                   SeckillOrderProducer producer,
                                   SnowflakeIdGenerator idGenerator) {
        this.redisTemplate = redisTemplate;
        this.inventoryMapper = inventoryMapper;
        this.orderMapper = orderMapper;
        this.producer = producer;
        this.idGenerator = idGenerator;
    }

    @Override
    public String submitSeckill(Long userId, Long productId) {
        String onceKey = IDEMPOTENT_KEY + userId + ":" + productId;
        String existing = redisTemplate.opsForValue().get(onceKey);
        if (existing != null) {
            if (existing.startsWith("SUCCESS:")) {
                return existing.substring("SUCCESS:".length());
            }
            throw new IllegalArgumentException("already submitted seckill order");
        }

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(onceKey, "PENDING", Duration.ofMinutes(10));
        if (!Boolean.TRUE.equals(locked)) {
            throw new IllegalArgumentException("duplicate seckill request");
        }

        String stockKey = STOCK_KEY + productId;
        if (redisTemplate.opsForValue().get(stockKey) == null) {
            Integer dbStock = inventoryMapper.findAvailableStock(productId);
            if (dbStock == null) {
                redisTemplate.delete(onceKey);
                throw new IllegalArgumentException("product inventory not found");
            }
            redisTemplate.opsForValue().set(stockKey, String.valueOf(dbStock), Duration.ofHours(1));
        }

        Long stockAfter = redisTemplate.opsForValue().decrement(stockKey);
        if (stockAfter == null || stockAfter < 0) {
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.delete(onceKey);
            throw new IllegalArgumentException("sold out");
        }

        String orderNo = String.valueOf(idGenerator.nextId());
        redisTemplate.opsForValue().set(onceKey, "PENDING:" + orderNo, Duration.ofMinutes(30));

        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setProductId(productId);
        try {
            producer.sendSeckill(message);
        } catch (Exception ex) {
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.delete(onceKey);
            throw new IllegalStateException("submit seckill failed, message send error", ex);
        }

        return orderNo;
    }

    @Override
    public void payOrder(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("order not found");
        }
        producer.sendPay(orderNo);
    }

    @Override
    public void cancelOrder(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("order not found");
        }
        producer.sendCancel(orderNo);
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }

    @Override
    public Order getByUserAndProduct(Long userId, Long productId) {
        return orderMapper.findByUserAndProduct(userId, productId);
    }
}
