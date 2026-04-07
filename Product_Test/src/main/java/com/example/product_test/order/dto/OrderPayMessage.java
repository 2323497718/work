package com.example.product_test.order.dto;

public class OrderPayMessage {
    private String orderNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String encode() {
        return orderNo;
    }

    public static OrderPayMessage decode(String payload) {
        OrderPayMessage message = new OrderPayMessage();
        message.setOrderNo(payload);
        return message;
    }
}
