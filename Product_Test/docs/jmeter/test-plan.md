# JMeter 压测步骤

## 一、准备

1. 启动容器：
   - `docker compose up --build -d`
2. 确认服务：
   - `http://localhost/`
   - `http://localhost/api/products/1`

## 二、动态接口压测（负载均衡）

1. 创建 Test Plan
2. 添加 Thread Group：
   - Threads: 200
   - Ramp-Up: 20
   - Loop Count: 20
3. 添加 HTTP Request：
   - Method: GET
   - Server: `localhost`
   - Port: `80`
   - Path: `/api/products/1`
4. 添加监听器：
   - Summary Report
   - Aggregate Report

观察：
- 平均响应时间（Average）
- 95% 响应时间（95% Line）
- 吞吐量（Throughput）
- 错误率（Error %）

验证均衡：
- 查看 `backend-1` 与 `backend-2` 日志中 `instance=... count=...`
- 或分别调用各实例的 `/api/system/stats` 对比计数

## 三、静态资源压测（动静分离）

1. 新建 Thread Group（同上参数）
2. HTTP Request：
   - Method: GET
   - Server: `localhost`
   - Port: `80`
   - Path: `/static/styles.css`
3. 对比动态接口报告

预期：
- 静态资源平均响应时间更低
- 吞吐量更高

## 四、缓存效果验证

1. 先执行一轮低并发请求 `/api/products/1`
2. 再执行同等并发压测
3. 对比首轮与后续轮次响应时间，命中缓存后应明显下降
