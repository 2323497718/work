# Product_Test

商品库存与秒杀系统课程作业示例工程。

## 快速启动

1. 创建数据库并执行脚本：
   - `src/main/resources/sql/schema.sql`
2. 修改数据库连接：
   - `src/main/resources/application.properties`
3. 启动项目：
   - Windows: `gradlew.bat bootRun`
   - Mac/Linux: `./gradlew bootRun`

## 当前已实现功能

- 用户注册：`POST /api/users/register`
- 用户登录：`POST /api/users/login`

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

## 设计文档

详见 `docs/system-design.md`，包含：
- 系统架构草图（用户/商品/订单/库存服务）
- RESTful API 定义
- 数据库 ER 图
- 技术栈选型说明
