# 高并发读作业说明

## 1. 容器环境

- `Dockerfile`：后端服务镜像构建
- `docker-compose.yml`：一键启动 MySQL、Redis、后端双实例、Nginx
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

## 5. 关键验证点

1. 首次查商品 -> 回源数据库
2. 重复查商品 -> 命中 Redis，响应更快
3. 压测时两台后端请求数大致均衡（轮询/最少连接）
