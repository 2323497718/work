# 高并发读作业说明

## 1. 容器环境

- `Dockerfile`：后端服务镜像构建
- `docker-compose.yml`：一键启动 MySQL 写库、MySQL 读库、Redis、后端双实例、Nginx
- Nginx 配置：
  - `docker/nginx/conf.d/default.conf`（轮询）
  - `docker/nginx/conf.d/least-conn.conf`（最少连接）
  - `docker/nginx/conf.d/ip-hash.conf`（IP 哈希）

启动命令：

```bash
docker compose up --build -d
```

## 2. 负载均衡

- 后端实例：
  - `backend-1` -> `8081`
  - `backend-2` -> `8082`
- 对外统一入口：Nginx `80` 端口
- 验证分流：
  1. 使用 JMeter 压测 `http://localhost/api/products/1`
  2. 查看后端日志中 `instance=backend-1/backend-2` 计数
  3. 或调用 `GET /api/system/stats` 分别检查请求计数

## 3. 动静分离

- 静态资源：`docker/nginx/html/static/*`
- 页面入口：`docker/nginx/html/index.html`
- 动态接口：`/api/*` 转发到后端
- 压测建议：
  - 静态：`GET http://localhost/static/styles.css`
  - 动态：`GET http://localhost/api/products/1`
  - 对比响应时间和吞吐

## 4. 分布式缓存

商品详情缓存接口：`GET /api/products/{id}`

处理策略：
- 穿透：DB 不存在时缓存空值（短 TTL）
- 击穿：热点 Key 重建使用 Redis 锁
- 雪崩：随机过期时间（基准 TTL + 抖动）

## 5. 读写分离（MySQL）

- 数据源配置：
  - 写库：`app.datasource.write.*`
  - 读库：`app.datasource.read.*`
- 路由方式：
  - `get/find/list/search` 开头的方法自动走读库
  - 其他方法走写库
- 代码验证点：
  - 写请求：`POST /api/products`
  - 读请求：`GET /api/products/{id}`、`GET /api/products/search?keyword=...`
- 观察日志关键字：
  - `db-route=WRITE method=createProduct`
  - `db-route=READ method=getProductById`

> 说明：本作业环境使用“独立读库容器”演示读写路由。若要实现主从实时同步，可在此基础上再配置 MySQL Replication。

## 6. ElasticSearch（可选）

- `docker-compose.yml` 已提供可选 `elasticsearch` 服务（`profile=es`）
- 启动命令：
  - `docker compose --profile es up -d`
- 当前已实现 `GET /api/products/search`（基于 MySQL `LIKE`），可作为接入 ES 前的对照版本

## 7. 关键验证点

1. 首次查商品 -> 回源数据库
2. 重复查商品 -> 命中 Redis，响应更快
3. 压测时两台后端请求数大致均衡（轮询/最少连接）
4. 商品新增请求命中写库，查询请求命中读库（日志可见）
