# Product_Test

商品库存与秒杀系统课程作业示例工程。

## 容器化启动（数据库 + Redis + 后端双实例 + Nginx）

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
- 请求统计：`GET /api/system/stats`

## 分布式缓存（Redis）

商品详情缓存已接入 Redis，策略如下：
- 缓存穿透：空对象缓存（`NULL` 占位 + 短 TTL）
- 缓存击穿：互斥锁（`SETNX + 过期时间`）保护热点 Key 回源
- 缓存雪崩：基础 TTL + 随机抖动，避免同一时刻大面积失效

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
