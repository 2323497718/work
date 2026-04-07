package com.example.product_test.order.dto;

public class SeckillOrderMessage {
    private String orderNo;
    private Long userId;
    private Long productId;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String encode() {
        return orderNo + "," + userId + "," + productId;
    }

    public static SeckillOrderMessage decode(String value) {
        String[] arr = value.split(",", -1);
        if (arr.length != 3) {
            throw new IllegalArgumentException("invalid seckill message");
        }
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderNo(arr[0]);
        message.setUserId(Long.parseLong(arr[1]));
        message.setProductId(Long.parseLong(arr[2]));
        return message;
    }
}
