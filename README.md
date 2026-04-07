# Product_Test

商品库存与秒杀系统课程作业示例工程。

## 容器化启动（写库 + 读库 + Redis + Kafka + 后端双实例 + Nginx）

1. 构建并启动：
   - `docker compose up --build -d`
2. 访问入口：
   - Nginx 首页（静态）：`http://localhost/`
   - 后端实例1：`http://localhost:8081`
   - 后端实例2：`http://localhost:8082`
3. 关闭：
   - `docker compose down`

## 负载均衡配置

- 默认配置文件：`docker/nginx/conf.d/default.conf`（Round Robin）
- 可选算法：
  - `docker/nginx/conf.d/least-conn.conf`
  - `docker/nginx/conf.d/ip-hash.conf`
- 切换方式：把 `docker-compose.yml` 中 Nginx 挂载的配置文件替换为目标文件后重启 Nginx。

## 动静分离

- 静态资源目录：`docker/nginx/html/static/`
- 静态页面：`docker/nginx/html/index.html`
- Nginx 直接处理 `/`、`/static/*`
- Nginx 代理动态接口 `/api/*` 到后端集群

- 用户注册：`POST /api/users/register`
- 用户登录：`POST /api/users/login`
- 商品详情：`GET /api/products/{id}`
- 商品搜索：`GET /api/products/search?keyword=demo`
- 商品新增：`POST /api/products`
- 秒杀下单：`POST /api/seckill/order?userId=1&productId=1`
- 查询订单：`GET /api/seckill/orders/{orderNo}`
- 请求统计：`GET /api/system/stats`

## 分布式缓存（Redis）

商品详情缓存已接入 Redis，策略如下：
- 缓存穿透：空对象缓存（`NULL` 占位 + 短 TTL）
- 缓存击穿：互斥锁（`SETNX + 过期时间`）保护热点 Key 回源
- 缓存雪崩：基础 TTL + 随机抖动，避免同一时刻大面积失效

## 读写分离（MySQL）

- 双数据源路由：`WRITE` / `READ`
- 读方法（`get/find/list/search`）自动走读库
- 写方法（如创建/更新）走写库
- 可通过应用日志中的 `db-route=READ/WRITE` 验证

## ElasticSearch（可选）

- `docker-compose.yml` 内置可选 ES 服务（profile: `es`）
- 启动方式：`docker compose --profile es up -d`

## 高并发写（秒杀）

- Redis 预扣库存，减少数据库压力
- Kafka 异步创建订单，削峰填谷
- 幂等防重：同一用户同一商品只能秒杀一次
- 雪花算法生成订单号
- 数据一致性：库存条件更新 + 补偿逻辑防超卖

## 分布式事务（消息最终一致性）

- 秒杀请求：Redis 预扣库存 + 用户限购
- 下单一致性：Kafka 异步下单消息，消费端事务落单+锁定库存
- 支付一致性：Kafka 支付消息，订单状态更新并确认库存
- 消费幂等：`tx_message_log` 去重，避免重复消费副作用

### 请求示例

注册：

```json
{
  "username": "test_user",
  "password": "12345678",
  "email": "test@example.com"
}
```

登录：

```json
{
  "username": "test_user",
  "password": "12345678"
}
```

## JMeter 压测

详见 `docs/high-concurrency-read.md` 与 `docs/jmeter/test-plan.md`。

## 设计文档

- `docs/system-design.md`
- `docs/high-concurrency-read.md`
- `docs/high-concurrency-write.md`
- `docs/distributed-transaction.md`
