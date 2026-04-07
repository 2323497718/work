package com.example.product_test.order.mq;

import com.example.product_test.order.dto.SeckillOrderMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.seckill-order}")
    private String seckillTopic;
    @Value("${app.kafka.topic.order-pay}")
    private String payTopic;
    @Value("${app.kafka.topic.order-cancel}")
    private String cancelTopic;

    public SeckillOrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendSeckill(SeckillOrderMessage message) {
        kafkaTemplate.send(seckillTopic, message.getOrderNo(), message.encode());
    }

    public void sendPay(String orderNo) {
        kafkaTemplate.send(payTopic, orderNo, orderNo);
    }

    public void sendCancel(String orderNo) {
        kafkaTemplate.send(cancelTopic, orderNo, orderNo);
    }
}
