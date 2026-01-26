# Doris 示例模块

本模块演示如何在生产环境中深度使用 Apache Doris OLAP 数据库，重点展示与 ClickHouse 的差异化功能。

## ⚠️ 重要说明

**Doris 是 OLAP（在线分析处理）数据库，不是 OLTP（在线事务处理）数据库**

### 适用场景 ✅
- 实时数据更新和删除
- 多表 JOIN 查询（复杂关联分析）
- 高并发查询场景
- MySQL 协议兼容（易于迁移）
- 实时数仓场景

### 不适用场景 ❌
- 高频事务处理（虽然支持更新，但不是为高频事务设计）
- 单条记录频繁更新（批量更新更合适）

## Doris vs ClickHouse 核心差异

| 特性 | ClickHouse | Doris |
|------|-----------|-------|
| **SQL 兼容性** | 非标准 SQL | ✅ MySQL 兼容 |
| **实时更新** | 主要追加 | ✅ 支持 UPDATE/DELETE |
| **多表 JOIN** | 性能一般 | ✅ CBO 优化，性能优异 |
| **并发性能** | 中等 | ✅ 高并发支持 |
| **物化视图** | 单表 | ✅ 多表物化视图 |
| **查询重写** | 不支持 | ✅ 自动查询重写 |
| **协议** | HTTP/原生 | ✅ MySQL 协议 |

## 功能特性

### 1. 表模型（生产环境核心）

#### Unique 模型
- ✅ 支持主键唯一性约束
- ✅ 支持实时更新（UPDATE/DELETE）
- ✅ 适合需要更新的场景（如订单状态）

#### Duplicate 模型
- ✅ 保留所有数据，不进行预聚合
- ✅ 适合明细数据存储

#### Aggregate 模型
- ✅ 自动预聚合
- ✅ 适合统计场景
- ✅ 减少存储空间

### 2. 实时更新（Doris 核心优势）

- ✅ UPDATE 操作（更新订单状态等）
- ✅ DELETE 操作（删除数据）
- ✅ 支持事务（有限支持）

### 3. 多表 JOIN 查询（Doris 核心优势）

- ✅ CBO（Cost-Based Optimizer）优化
- ✅ 复杂 JOIN 性能优异
- ✅ 支持多种 JOIN 类型

### 4. 物化视图

- ✅ 多表物化视图
- ✅ 自动查询重写
- ✅ 显著提高查询性能

### 5. MySQL 协议兼容

- ✅ 使用标准 MySQL JDBC 驱动
- ✅ 兼容 MySQL 客户端工具
- ✅ 易于从 MySQL 迁移

## 快速开始

### 1. 启动 Doris 服务

使用 Docker Compose 启动 Doris：

```bash
docker-compose up -d doris-fe doris-be
```

**注意**：Doris 启动时间较长（1-2分钟），请耐心等待。

Doris 连接信息：
- FE（Frontend）端口：`9030`（MySQL 协议，应用连接此端口）
- FE Web UI：`http://localhost:8030`
- BE（Backend）端口：`8040`（HTTP 接口）
- 默认用户：`root`（默认密码：空或 `root`）

### 2. 将 BE 添加到 FE 集群

**重要**：BE 启动后需要手动添加到 FE 集群。

**方式1：通过 SQL（推荐）**
```bash
# 连接到 FE
mysql -h localhost -P 9030 -uroot

# 添加 BE 节点
ALTER SYSTEM ADD BACKEND "doris-be:9050";
```

**方式2：通过 FE Web UI**
1. 访问 http://localhost:8030
2. 登录（用户名：root，密码：空或 root）
3. 进入「集群管理」->「Backend」
4. 点击「添加 Backend」
5. 输入：`doris-be:9050`

### 3. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS demo;
```

### 3. 启动应用

应用启动时会自动创建表：

```bash
cd doris-example
mvn spring-boot:run
```

### 4. 创建物化视图（可选）

```bash
curl -X POST http://localhost:8089/doris/tables/materialized-view
```

### 5. 测试实时更新

```bash
# 更新订单状态（展示Doris实时更新能力）
curl -X PUT "http://localhost:8089/doris/data/orders/123/status?status=shipped"
```

## API 接口

### 表管理

#### 1. 创建用户表（Duplicate模型）
```bash
POST /doris/tables/users
```

#### 2. 创建订单表（Unique模型，支持实时更新）
```bash
POST /doris/tables/orders
```

#### 3. 创建订单汇总表（Aggregate模型）
```bash
POST /doris/tables/order-summary
```

#### 4. 创建物化视图（多表物化视图）
```bash
POST /doris/tables/materialized-view
```

### 数据导入

#### 1. 批量插入用户
```bash
POST /doris/data/users/batch
Content-Type: application/json

[
  {
    "userId": 1,
    "username": "john",
    "email": "john@example.com",
    "city": "Beijing",
    "country": "China",
    "registerTime": "2024-01-01T10:00:00",
    "age": 25,
    "gender": "male"
  }
]
```

#### 2. 批量插入订单
```bash
POST /doris/data/orders/batch
Content-Type: application/json

[
  {
    "orderId": 1,
    "userId": 1,
    "productName": "Product A",
    "amount": 99.99,
    "status": "pending",
    "orderTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
]
```

### 实时更新（Doris核心优势）

#### 1. 更新用户信息
```bash
PUT /doris/data/users/{userId}?email=new@example.com&city=Shanghai
```

响应示例：
```json
{
  "success": true,
  "updatedCount": 1,
  "message": "更新用户成功",
  "note": "Doris支持实时UPDATE操作（ClickHouse主要支持追加）"
}
```

#### 2. 更新订单状态
```bash
PUT /doris/data/orders/{orderId}/status?status=shipped
```

#### 3. 删除用户
```bash
DELETE /doris/data/users/{userId}
```

#### 4. 删除订单
```bash
DELETE /doris/data/orders/{orderId}
```

### 多表JOIN查询（Doris核心优势）

#### 1. 查询用户订单
```bash
GET /doris/query/user-orders/{userId}
```

响应示例：
```json
{
  "success": true,
  "userId": 1,
  "orders": [
    {
      "user_id": 1,
      "username": "john",
      "city": "Beijing",
      "order_id": 1,
      "product_name": "Product A",
      "amount": 99.99,
      "status": "shipped",
      "order_time": "2024-01-01 10:00:00"
    }
  ],
  "count": 1,
  "note": "Doris的CBO优化器使多表JOIN性能优于ClickHouse"
}
```

#### 2. 按城市统计订单（多表JOIN + 聚合）
```bash
GET /doris/query/stats/city?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

#### 3. 用户订单汇总
```bash
GET /doris/query/user-summary?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

#### 4. 复杂JOIN查询
```bash
GET /doris/query/complex-join
```

### 物化视图查询

#### 1. 从物化视图查询（自动查询重写）
```bash
GET /doris/query/materialized-view?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

### 统计接口

#### 1. 获取用户统计
```bash
GET /doris/stats/users
```

#### 2. 获取订单统计
```bash
GET /doris/stats/orders?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

## 配置说明

### application.yml 配置项

```yaml
doris:
  # JDBC连接URL（使用MySQL协议）
  url: jdbc:mysql://localhost:9030/demo
  username: root
  password: ""
  database: demo
```

### Doris 连接信息

- **FE（Frontend）**：`localhost:9030` - MySQL 协议端口
- **BE（Backend）**：`localhost:8040` - HTTP 接口端口（可选）

## 生产环境最佳实践

### 1. 表模型选择

#### Unique 模型（推荐用于需要更新的场景）
```sql
CREATE TABLE orders (
    order_id BIGINT NOT NULL,
    status VARCHAR(20),
    ...
) ENGINE=OLAP
UNIQUE KEY(order_id)
...
PROPERTIES (
    "enable_unique_key_merge_on_write" = "true"  -- 启用实时更新
)
```

**适用场景**：
- 订单表（需要更新状态）
- 用户表（需要更新信息）
- 需要实时更新的场景

#### Duplicate 模型（推荐用于明细数据）
```sql
CREATE TABLE users (
    user_id BIGINT NOT NULL,
    ...
) ENGINE=OLAP
DUPLICATE KEY(user_id)
...
```

**适用场景**：
- 日志表
- 明细数据表
- 不需要更新的场景

#### Aggregate 模型（推荐用于统计场景）
```sql
CREATE TABLE order_summary (
    user_id BIGINT,
    order_date DATE,
    total_orders BIGINT SUM,
    total_amount DECIMAL(10, 2) SUM
) ENGINE=OLAP
AGGREGATE KEY(user_id, order_date)
...
```

**适用场景**：
- 统计汇总表
- 需要预聚合的场景

### 2. 实时更新优化

#### 启用 Merge-On-Write
```sql
PROPERTIES (
    "enable_unique_key_merge_on_write" = "true"
)
```

**优势**：
- 写入时合并，查询时无需 FINAL
- 查询性能更好
- 适合实时更新场景

### 3. 多表JOIN优化

#### 使用 CBO 优化器
- Doris 自动使用 CBO 优化 JOIN 顺序
- 根据统计信息选择最优执行计划
- 无需手动优化

#### JOIN 类型选择
- **INNER JOIN**：内连接
- **LEFT JOIN**：左连接
- **RIGHT JOIN**：右连接
- **FULL OUTER JOIN**：全外连接

### 4. 物化视图设计

#### 多表物化视图
```sql
CREATE MATERIALIZED VIEW user_order_mv
AS
SELECT
    u.user_id,
    u.username,
    COUNT(o.order_id) AS order_count,
    SUM(o.amount) AS total_amount
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.username
```

**优势**：
- 自动查询重写
- 无需修改 SQL
- 显著提高性能

### 5. 分区和分桶

#### 分区策略
```sql
PARTITION BY RANGE(order_time) (
    PARTITION p202401 VALUES [("2024-01-01"), ("2024-02-01")),
    PARTITION p202402 VALUES [("2024-02-01"), ("2024-03-01"))
)
```

#### 分桶策略
```sql
DISTRIBUTED BY HASH(order_id) BUCKETS 10
```

**建议**：
- 分区：按时间分区（提高查询效率）
- 分桶：按主键或常用查询字段分桶

## Doris vs ClickHouse 详细对比

### 1. SQL 兼容性

**Doris**：
- ✅ MySQL 协议兼容
- ✅ 标准 SQL 语法
- ✅ 易于从 MySQL 迁移

**ClickHouse**：
- ⚠️ 非标准 SQL
- ⚠️ 语法差异较大
- ⚠️ 学习成本较高

### 2. 实时更新

**Doris**：
- ✅ 支持 UPDATE 操作
- ✅ 支持 DELETE 操作
- ✅ Merge-On-Write 模式性能好

**ClickHouse**：
- ❌ 主要支持追加
- ⚠️ 更新需要重建表或使用 FINAL（性能差）

### 3. 多表 JOIN

**Doris**：
- ✅ CBO 优化器
- ✅ 复杂 JOIN 性能优异
- ✅ 支持多种 JOIN 类型

**ClickHouse**：
- ⚠️ JOIN 性能一般
- ⚠️ 缺乏 CBO 优化
- ⚠️ 复杂 JOIN 性能较差

### 4. 并发性能

**Doris**：
- ✅ 支持高并发查询
- ✅ 连接池可以设置较大

**ClickHouse**：
- ⚠️ 并发性能中等
- ⚠️ 连接池建议较小

### 5. 物化视图

**Doris**：
- ✅ 支持多表物化视图
- ✅ 自动查询重写

**ClickHouse**：
- ⚠️ 仅支持单表物化视图
- ❌ 不支持查询重写

### 6. 使用场景对比

| 场景 | ClickHouse | Doris |
|------|-----------|-------|
| **日志分析** | ✅ 优秀 | ✅ 良好 |
| **时序数据** | ✅ 优秀 | ✅ 良好 |
| **实时更新** | ❌ 不支持 | ✅ 优秀 |
| **多表JOIN** | ⚠️ 一般 | ✅ 优秀 |
| **高并发** | ⚠️ 中等 | ✅ 优秀 |
| **MySQL迁移** | ❌ 困难 | ✅ 容易 |

## 典型使用场景

### 1. 实时数仓

```java
// 实时更新订单状态
dataService.updateOrderStatus(orderId, "shipped");

// 查询用户订单（多表JOIN）
List<Map<String, Object>> orders = dataService.getUserOrders(userId);
```

### 2. 复杂分析查询

```java
// 按城市统计订单（多表JOIN + 聚合）
List<Map<String, Object>> stats = dataService.getOrderStatsByCity(startTime, endTime);
```

### 3. 物化视图加速

```java
// 从物化视图查询（自动查询重写）
List<Map<String, Object>> data = dataService.queryFromMaterializedView(startTime, endTime);
```

## 常见问题

### Q1: Doris 和 ClickHouse 如何选择？

**A**: 
- **选择 ClickHouse**：单表分析、日志分析、时序数据、不需要更新
- **选择 Doris**：需要实时更新、多表JOIN、高并发、MySQL迁移

### Q2: Doris 的 UPDATE 性能如何？

**A**: 
- Unique 模型 + Merge-On-Write 模式下性能较好
- 适合批量更新，不适合高频单条更新
- 比 ClickHouse 的 FINAL 查询性能好很多

### Q3: 如何优化多表 JOIN 查询？

**A**: 
1. Doris 的 CBO 会自动优化
2. 创建合适的物化视图
3. 合理设计分区和分桶
4. 使用合适的 JOIN 类型

### Q4: MySQL 迁移到 Doris 容易吗？

**A**: 
- ✅ 非常容易
- ✅ 使用相同的 MySQL JDBC 驱动
- ✅ SQL 语法基本兼容
- ✅ 可以逐步迁移

## 参考资源

- [Apache Doris 官方文档](https://doris.apache.org/docs/)
- [Doris vs ClickHouse 对比](https://doris.apache.org/docs/dev/gettingStarted/alternatives/alternative-to-clickhouse/)
- [Doris 最佳实践](https://doris.apache.org/docs/dev/best-practices/)
