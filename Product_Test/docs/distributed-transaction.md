# 分布式事务作业说明

## 场景

订单服务与库存服务逻辑上独立，各自维护本地数据状态。秒杀流程使用 Redis + Kafka 实现最终一致性。

## 1. 秒杀下单（库存预扣减、防超卖、限购）

- Redis 预扣减库存：`seckill:stock:{productId}`
- 用户限购/幂等：`seckill:once:{userId}:{productId}`
- 同一用户同一商品只允许一次秒杀（DB 也有唯一索引兜底）

## 2. 基于消息的一致性（最终一致）

### 2.1 下单 + 库存扣减一致性

1. 接口接收请求后先在 Redis 预扣库存
2. 发送 Kafka 下单消息（`seckill-order-topic`）
3. 消费端事务内执行：
   - 库存表 `available_stock - 1, locked_stock + 1`
   - 创建订单，状态 `UNPAID(0)`
4. 若失败触发补偿：恢复 Redis 库存并清理幂等占位

### 2.2 订单支付 + 状态更新一致性

1. 支付请求发送 Kafka 支付消息（`order-pay-topic`）
2. 消费端事务内执行：
   - 订单状态 `UNPAID -> PAID`
   - 库存表 `locked_stock - 1`（锁定库存转成交）

### 2.3 取消订单一致性

1. 取消请求发送 Kafka 取消消息（`order-cancel-topic`）
2. 消费端事务内执行：
   - 订单状态 `UNPAID -> CANCELED`
   - 库存回滚：`available_stock + 1, locked_stock - 1`
   - Redis 库存与幂等键同步回滚

## 3. 消费幂等与重复消息处理

- 表：`tx_message_log`
- 消费前 `INSERT IGNORE` 记录 `msg_key`，若已存在则直接跳过
- 避免 Kafka 重投导致重复扣减和重复改状态

## 4. 核心接口

- 秒杀下单：`POST /api/seckill/order?userId=1&productId=1`
- 支付订单：`POST /api/seckill/pay?orderNo=...`
- 取消订单：`POST /api/seckill/cancel?orderNo=...`
- 查订单号：`GET /api/seckill/orders/{orderNo}`
- 查用户商品单：`GET /api/seckill/orders/user/{userId}?productId=1`

## 5. 关键结论

- 防超卖：Redis 预扣 + DB 条件更新双重保障
- 限购：Redis 幂等键 + DB 唯一索引
- 一致性：基于消息驱动的最终一致性
- 可恢复：失败补偿与消息幂等保证系统稳定
