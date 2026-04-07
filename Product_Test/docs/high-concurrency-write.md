# 高并发写作业说明

## 1. 秒杀下单架构

核心链路：

1. 请求进入 `POST /api/seckill/order`
2. Redis 校验幂等（同一用户同一商品只允许一次）并预扣库存
3. 生成雪花订单号
4. 发送 Kafka 消息（异步削峰）
5. 消费端创建订单并落库，更新库存状态
6. 查询接口按订单号/用户+商品查询结果

## 2. 关键能力实现

### 2.1 消息队列异步下单
- 生产者：`order/mq/SeckillOrderProducer`
- 消费者：`order/mq/SeckillOrderConsumer`
- Topic 配置：`app.kafka.topic.seckill-order`
- 目的：削峰填谷，避免下单请求直接压垮数据库

### 2.2 Redis 库存缓存
- Key：`seckill:stock:{productId}`
- 秒杀时先在 Redis 做库存递减
- 库存不足立即失败，避免无效请求进入后端

### 2.3 幂等防重复下单
- Key：`seckill:once:{userId}:{productId}`
- 同一用户同一商品只允许一次秒杀
- 数据库层额外约束：`orders` 表唯一索引 `uk_user_product(user_id, product_id)`

### 2.4 订单ID（雪花算法）
- 实现类：`order/util/SnowflakeIdGenerator`
- 下单时生成全局唯一 `orderNo`

### 2.5 数据一致性（防超卖）
- Redis 预扣库存
- 消费端数据库 `inventory` 条件更新：`available_stock > 0` 才能扣减
- 失败时执行补偿：恢复 Redis 库存并清理幂等占位
- 最终保证库存不超卖，订单数据完整

## 3. API 列表

- `POST /api/seckill/order?userId=1&productId=1`：提交秒杀
- `GET /api/seckill/orders/{orderNo}`：按订单号查询
- `GET /api/seckill/orders/user/{userId}?productId=1`：按用户+商品查询（用于幂等验证）

## 4. 容器依赖

`docker-compose.yml` 已加入 Kafka：

- `kafka`（bitnami/kafka，KRaft 单节点）
- 后端自动读取 `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

启动命令：

```bash
docker compose up --build -d
```

## 5. 验证建议

1. 准备一个用户（先走注册接口）
2. 并发压测 `POST /api/seckill/order`
3. 验证：
   - 同用户重复下单被拦截
   - `inventory.available_stock` 不会小于 0
   - `orders` 中同用户同商品最多 1 条
   - Kafka 消费日志有订单创建记录
