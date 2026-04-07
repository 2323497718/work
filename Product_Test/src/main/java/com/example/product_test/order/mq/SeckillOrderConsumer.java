package com.example.product_test.order.mq;

import com.example.product_test.order.dto.OrderPayMessage;
import com.example.product_test.order.dto.SeckillOrderMessage;
import com.example.product_test.order.mapper.InventoryMapper;
import com.example.product_test.order.mapper.OrderMapper;
import com.example.product_test.order.mapper.TxMessageLogMapper;
import com.example.product_test.order.model.Order;
import com.example.product_test.product.mapper.ProductMapper;
import com.example.product_test.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class SeckillOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);
    private static final String STOCK_KEY = "seckill:stock:";
    private static final String IDEMPOTENT_KEY = "seckill:once:";
    private static final int ORDER_STATUS_UNPAID = 0;
    private static final int ORDER_STATUS_PAID = 1;
    private static final int ORDER_STATUS_CANCELED = 2;

    private final OrderMapper orderMapper;
    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final TxMessageLogMapper txMessageLogMapper;

    @Value("${app.kafka.topic.seckill-order}")
    private String orderTopic;
    @Value("${app.kafka.topic.order-pay}")
    private String payTopic;
    @Value("${app.kafka.topic.order-cancel}")
    private String cancelTopic;

    public SeckillOrderConsumer(OrderMapper orderMapper,
                                InventoryMapper inventoryMapper,
                                ProductMapper productMapper,
                                StringRedisTemplate redisTemplate,
                                TxMessageLogMapper txMessageLogMapper) {
        this.orderMapper = orderMapper;
        this.inventoryMapper = inventoryMapper;
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
        this.txMessageLogMapper = txMessageLogMapper;
    }

    @KafkaListener(topics = "${app.kafka.topic.seckill-order}")
    public void consume(String payload) {
        SeckillOrderMessage message = SeckillOrderMessage.decode(payload);
        try {
            int inserted = txMessageLogMapper.insertIgnore("ORDER_CREATE:" + message.getOrderNo(), orderTopic);
            if (inserted <= 0) {
                return;
            }
            createOrderTx(message);
        } catch (Exception ex) {
            String onceKey = IDEMPOTENT_KEY + message.getUserId() + ":" + message.getProductId();
            redisTemplate.delete(onceKey);
            redisTemplate.opsForValue().increment(STOCK_KEY + message.getProductId());
            log.error("seckill consume failed, payload={}", payload, ex);
        }
    }

    @Transactional
    public void createOrderTx(SeckillOrderMessage message) {
        String onceKey = IDEMPOTENT_KEY + message.getUserId() + ":" + message.getProductId();
        Order existing = orderMapper.findByUserAndProduct(message.getUserId(), message.getProductId());
        if (existing != null) {
            redisTemplate.opsForValue().set(onceKey, "SUCCESS:" + existing.getOrderNo(), Duration.ofDays(1));
            return;
        }

        int updated = inventoryMapper.reserveStock(message.getProductId());
        if (updated <= 0) {
            redisTemplate.delete(onceKey);
            redisTemplate.opsForValue().increment(STOCK_KEY + message.getProductId());
            return;
        }

        Product product = productMapper.findById(message.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("product not found in consumer");
        }

        Order order = new Order();
        order.setOrderNo(message.getOrderNo());
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setQuantity(1);
        order.setAmount(product.getPrice());
        order.setOrderStatus(ORDER_STATUS_UNPAID);
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            Order duplicate = orderMapper.findByUserAndProduct(message.getUserId(), message.getProductId());
            if (duplicate != null) {
                redisTemplate.opsForValue().set(onceKey, "SUCCESS:" + duplicate.getOrderNo(), Duration.ofDays(1));
                return;
            }
            throw ex;
        }

        redisTemplate.opsForValue().set(onceKey, "SUCCESS:" + message.getOrderNo(), Duration.ofDays(1));
        log.info("seckill order created, topic={}, orderNo={}, userId={}, productId={}",
                orderTopic, message.getOrderNo(), message.getUserId(), message.getProductId());
    }

    @KafkaListener(topics = "${app.kafka.topic.order-pay}")
    public void consumePay(String payload) {
        OrderPayMessage message = OrderPayMessage.decode(payload);
        int inserted = txMessageLogMapper.insertIgnore("ORDER_PAY:" + message.getOrderNo(), payTopic);
        if (inserted <= 0) {
            return;
        }
        payOrderTx(message.getOrderNo());
    }

    @KafkaListener(topics = "${app.kafka.topic.order-cancel}")
    public void consumeCancel(String payload) {
        OrderPayMessage message = OrderPayMessage.decode(payload);
        int inserted = txMessageLogMapper.insertIgnore("ORDER_CANCEL:" + message.getOrderNo(), cancelTopic);
        if (inserted <= 0) {
            return;
        }
        cancelOrderTx(message.getOrderNo());
    }

    @Transactional
    public void payOrderTx(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        int changed = orderMapper.updateStatus(orderNo, ORDER_STATUS_UNPAID, ORDER_STATUS_PAID);
        if (changed <= 0) {
            return;
        }
        inventoryMapper.confirmPaid(order.getProductId());
        log.info("order paid and status updated, orderNo={}", orderNo);
    }

    @Transactional
    public void cancelOrderTx(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            return;
        }
        int changed = orderMapper.updateStatus(orderNo, ORDER_STATUS_UNPAID, ORDER_STATUS_CANCELED);
        if (changed <= 0) {
            return;
        }
        inventoryMapper.releaseLocked(order.getProductId());
        redisTemplate.opsForValue().increment(STOCK_KEY + order.getProductId());
        redisTemplate.delete(IDEMPOTENT_KEY + order.getUserId() + ":" + order.getProductId());
        log.info("order canceled and stock released, orderNo={}", orderNo);
    }
}
