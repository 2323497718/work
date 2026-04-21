# Nacos 服务注册发现与配置管理 - 测试指南

## 目录
1. [环境准备](#1-环境准备)
2. [启动服务](#2-启动服务)
3. [服务注册发现测试](#3-服务注册发现测试)
4. [配置中心测试](#4-配置中心测试)
5. [网关路由测试](#5-网关路由测试)
6. [流量治理测试](#6-流量治理测试)
7. [压力测试](#7-压力测试)
8. [常见问题](#8-常见问题)

---

## 1. 环境准备

### 1.1 前置条件
- Docker 和 Docker Compose
- JDK 25+
- JMeter 5.6+ (可选，用于压力测试)

### 1.2 启动基础服务

```bash
# 启动 MySQL、Redis、Kafka 和 Nacos
docker-compose -f docker-compose-nacos.yml up -d

# 验证 Nacos 是否启动成功
curl http://localhost:8848/nacos/v1/console/health/readiness
```

### 1.3 Nacos 控制台访问
- **地址**: http://localhost:8848/nacos
- **默认账号**: nacos / nacos

---

## 2. 启动服务

### 2.1 启动后端服务（主应用）

```bash
# 方式1：直接运行
./gradlew bootRun

# 方式2：打包后运行
./gradlew bootJar
java -jar build/libs/Product_Test-0.0.1-SNAPSHOT.jar
```

### 2.2 启动网关服务

```bash
# 进入 gateway 目录
cd gateway

# 构建网关
./gradlew build

# 运行网关
java -jar build/libs/gateway.jar
```

### 2.3 验证服务注册

访问 Nacos 控制台 http://localhost:8848/nacos，在「服务管理」→「服务列表」中应能看到：
- `product-test-service` (后端服务)
- `gateway-service` (网关服务)

---

## 3. 服务注册发现测试

### 3.1 API 端点

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/nacos/instance` | GET | 获取当前实例信息 |
| `/api/nacos/services` | GET | 获取所有注册的服务 |
| `/api/nacos/services/{serviceId}` | GET | 获取指定服务的实例列表 |

### 3.2 测试步骤

**步骤 1：获取所有注册的服务**

```bash
curl http://localhost:8081/api/nacos/services
```

**预期响应：**
```json
{
  "total": 2,
  "services": ["product-test-service", "gateway-service"],
  "timestamp": "2026-04-21T00:00:00Z"
}
```

**步骤 2：获取服务实例详情**

```bash
curl http://localhost:8081/api/nacos/services/product-test-service
```

**预期响应：**
```json
{
  "serviceId": "product-test-service",
  "total": 2,
  "instances": [
    {
      "instanceId": "192.168.1.100#8081",
      "host": "192.168.1.100",
      "port": 8081,
      "secure": false,
      "metadata": {}
    }
  ],
  "timestamp": "2026-04-21T00:00:00Z"
}
```

---

## 4. 配置中心测试

### 4.1 动态配置 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/nacos/config` | GET | 获取当前配置值 |
| `/api/nacos/config/rate-limit` | GET | 获取限流配置 |
| `/api/nacos/config/seckill` | GET | 获取秒杀配置 |
| `/actuator/refresh` | POST | 手动刷新配置 |

### 4.2 在 Nacos 中创建配置

1. 登录 Nacos 控制台
2. 进入「配置管理」→「配置列表」
3. 点击「+」创建新配置

**配置信息：**
- Data ID: `product-test-service.yaml`
- Group: `DEFAULT_GROUP`
- 配置格式: YAML

**配置内容：**
```yaml
app:
  features:
    rate-limit:
      enabled: true
      max-requests-per-second: 100
      burst-capacity: 200
    seckill:
      enabled: true
      max-quantity-per-order: 5
      seckill-duration-minutes: 60
```

### 4.3 测试动态刷新

**步骤 1：获取当前配置**

```bash
curl http://localhost:8081/api/nacos/config
```

**步骤 2：在 Nacos 控制台修改配置**
将 `max-requests-per-second` 从 100 改为 200

**步骤 3：手动刷新配置**

```bash
curl -X POST http://localhost:8081/actuator/refresh
```

**步骤 4：再次获取配置，验证值已更新**

```bash
curl http://localhost:8081/api/nacos/config/rate-limit
```

**预期响应：**
```json
{
  "maxRequestsPerSecond": 200,
  "burstCapacity": 200,
  "timestamp": "2026-04-21T00:00:00Z"
}
```

---

## 5. 网关路由测试

### 5.1 网关路由规则

| 路径 | 目标服务 | 说明 |
|------|----------|------|
| `/api/products/**` | product-service | 产品服务 |
| `/api/orders/**` | order-service | 订单服务 |
| `/api/users/**` | user-service | 用户服务 |
| `/api/backend/**` | backend-service | 后端服务 |

### 5.2 测试网关路由

**通过网关访问后端服务：**

```bash
# 直接访问后端
curl http://localhost:8081/api/nacos/config

# 通过网关访问（需要启动网关）
curl http://localhost:8080/api/backend/nacos/config
```

**测试负载均衡：**

```bash
# 多次请求，观察不同的实例响应
for i in {1..10}; do
  curl http://localhost:8080/api/backend/nacos/instance
  echo ""
done
```

**预期效果：** 看到不同的端口响应（8081, 8082）

### 5.3 验证动态路由

在 Nacos 控制台修改网关路由配置后，无需重启网关即可生效。

---

## 6. 流量治理测试

### 6.1 Sentinel 控制台

- **地址**: http://localhost:8858
- **默认账号**: sentinel / sentinel

### 6.2 测试限流

**步骤 1：发送大量请求**

```bash
# 使用 Apache Bench 测试
ab -n 1000 -c 100 http://localhost:8080/api/backend/nacos/config
```

**步骤 2：观察 Sentinel 控制台**
- 进入「实时监控」
- 查看通过的请求数和被限流的请求数

**步骤 3：验证限流响应**
超过阈值的请求应返回 429 状态码：

```json
{
  "success": false,
  "code": 429,
  "message": "请求过于频繁，请稍后重试"
}
```

### 6.3 测试熔断

**步骤 1：模拟服务故障**
停止后端服务或使其响应变慢

**步骤 2：发送请求触发熔断**
连续发送请求，触发异常比例阈值

**步骤 3：观察熔断状态**
在 Sentinel 控制台查看熔断器状态（CLOSED → OPEN → HALF-OPEN）

**步骤 4：验证降级响应**
熔断开启后，请求应返回降级响应：

```json
{
  "success": false,
  "code": 503,
  "message": "Product service is currently unavailable",
  "fallback": true
}
```

### 6.4 熔断规则说明

| 规则类型 | 阈值 | 熔断时长 |
|----------|------|----------|
| 异常比例 | 50% | 30秒 |
| 响应时间 | 2000ms | 60秒 |
| 异常数 | 10 | 60秒 |

---

## 7. 压力测试

### 7.1 使用 JMeter 测试

**步骤 1：打开 JMeter**

```bash
jmeter -t docs/jmeter/nacos-load-test.jmx
```

**步骤 2：修改测试参数**
根据实际情况修改 `User Defined Variables`：
- `GATEWAY_HOST`: 网关地址
- `GATEWAY_PORT`: 网关端口
- `SERVICE_HOST`: 服务地址
- `SERVICE_PORT`: 服务端口

**步骤 3：运行测试**
点击「运行」按钮开始测试

### 7.2 测试场景

| 场景 | 线程数 | 时长 | 说明 |
|------|--------|------|------|
| 服务发现测试 | 50 | 120秒 | 验证服务注册稳定性 |
| 动态配置测试 | 10 | 100次迭代 | 验证配置刷新能力 |
| 限流测试 | 100 | 60秒 | 验证限流效果 |
| 熔断测试 | 20 | 200次迭代 | 验证熔断机制 |

### 7.3 测试指标

关注以下指标：
- **吞吐量 (TPS)**: 每秒处理的请求数
- **响应时间**: 平均响应时间、P99 响应时间
- **错误率**: 请求失败的比例
- **限流拒绝率**: 被限流的请求比例

---

## 8. 常见问题

### Q1: Nacos 启动失败

**解决方案：**
```bash
# 查看日志
docker logs seckill-nacos

# 清理数据后重试
docker-compose -f docker-compose-nacos.yml down -v
docker-compose -f docker-compose-nacos.yml up -d
```

### Q2: 服务无法注册到 Nacos

**检查项：**
1. Nacos 是否正常运行
2. 网络是否互通
3. 配置是否正确

**解决方案：**
```bash
# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=product-test-service
```

### Q3: 配置不生效

**检查项：**
1. bootstrap.yml 是否正确配置
2. 配置的 Data ID 和 Group 是否匹配
3. 是否手动触发了刷新

**解决方案：**
```bash
# 手动刷新配置
curl -X POST http://localhost:8081/actuator/refresh
```

### Q4: 网关路由失败

**检查项：**
1. 目标服务是否已注册
2. 路由规则是否正确
3. 网络是否互通

**解决方案：**
```bash
# 查看网关日志
docker logs seckill-gateway

# 测试直接访问服务
curl http://localhost:8081/api/nacos/health
```

### Q5: Sentinel 限流不生效

**检查项：**
1. Sentinel 是否正常运行
2. 是否引入了 Sentinel 依赖
3. 限流规则是否正确配置

---

## 附录

### A. 相关端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 控制台 |
| Nacos | 9848 | gRPC 端口 |
| Sentinel | 8858 | 控制台 |
| Gateway | 8080 | 网关 |
| Backend | 8081/8082 | 后端服务 |
| MySQL | 3306 | 主库 |
| Redis | 6379 | 缓存 |

### B. API 快速测试脚本

```bash
#!/bin/bash

echo "=== Nacos 服务注册发现测试 ==="

echo "1. 检查服务实例..."
curl -s http://localhost:8081/api/nacos/instance | jq

echo -e "\n2. 获取所有服务..."
curl -s http://localhost:8081/api/nacos/services | jq

echo -e "\n3. 获取产品服务实例..."
curl -s http://localhost:8081/api/nacos/services/product-test-service | jq

echo -e "\n4. 获取动态配置..."
curl -s http://localhost:8081/api/nacos/config | jq

echo -e "\n5. 测试网关路由..."
curl -s http://localhost:8080/api/backend/nacos/config | jq

echo -e "\n=== 测试完成 ==="
```

---

**文档版本**: v1.0
**更新日期**: 2026-04-21
