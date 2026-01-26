# ClickHouse 示例模块

本模块演示如何在生产环境中深度使用 ClickHouse OLAP 数据库，包括批量数据导入、聚合分析、物化视图等核心功能。

## ⚠️ 重要说明

**ClickHouse 是 OLAP（在线分析处理）数据库，不是 OLTP（在线事务处理）数据库**

### 适用场景 ✅
- 批量数据导入（日志、事件数据）
- 聚合分析查询（统计、报表）
- 时间序列数据分析
- 实时数据分析
- 数据仓库场景

### 不适用场景 ❌
- 高频事务处理
- 单条记录频繁更新
- 复杂关联查询
- 实时写入单条数据

## 功能特性

### 1. 表设计（生产环境最佳实践）
- ✅ MergeTree 引擎（最常用）
- ✅ 按时间分区（提高查询效率）
- ✅ 排序键优化（ORDER BY）
- ✅ TTL 设置（数据生命周期管理）
- ✅ 索引优化

### 2. 批量数据导入
- ✅ JDBC 批量插入
- ✅ VALUES 格式批量插入（性能最优）
- ✅ 批量大小优化建议

### 3. 聚合分析查询
- ✅ 按事件类型统计
- ✅ 按时间维度统计（小时、天）
- ✅ 按设备类型统计
- ✅ TOP N 查询（热门页面）
- ✅ 用户行为路径分析

### 4. 性能优化
- ✅ 物化视图（预聚合）
- ✅ 分区管理
- ✅ 查询优化

### 5. 监控和管理
- ✅ 表统计信息
- ✅ 分区信息查询
- ✅ 数据生命周期监控

## 快速开始

### 1. 启动 ClickHouse 服务

使用 Docker Compose 启动 ClickHouse：

```bash
docker-compose up -d clickhouse
```

ClickHouse 连接信息：
- HTTP 端口：`8123`
- 原生协议端口：`9000`
- 默认用户：`default`（无密码）

### 2. 创建表

首次使用需要创建表：

```bash
curl -X POST http://localhost:8088/clickhouse/tables/user-behavior-log
```

### 3. 创建物化视图（可选，用于性能优化）

```bash
curl -X POST http://localhost:8088/clickhouse/tables/materialized-view
```

### 4. 启动应用

```bash
cd clickhouse-example
mvn spring-boot:run
```

### 5. 批量导入测试数据

```bash
curl -X POST http://localhost:8088/clickhouse/data/batch-insert \
  -H "Content-Type: application/json" \
  -d '[
    {
      "eventTime": "2024-01-01T10:00:00",
      "userId": "user1",
      "eventType": "page_view",
      "pageUrl": "/home",
      "duration": 30,
      "deviceType": "mobile",
      "ipAddress": "192.168.1.1"
    },
    {
      "eventTime": "2024-01-01T10:01:00",
      "userId": "user1",
      "eventType": "click",
      "pageUrl": "/product/123",
      "duration": 5,
      "deviceType": "mobile",
      "ipAddress": "192.168.1.1"
    }
  ]'
```

## API 接口

### 表管理

#### 1. 创建用户行为日志表
```bash
POST /clickhouse/tables/user-behavior-log
```

#### 2. 创建物化视图
```bash
POST /clickhouse/tables/materialized-view
```

#### 3. 获取表信息
```bash
GET /clickhouse/tables/{tableName}/info
```

### 数据导入

#### 1. 批量插入（JDBC方式）
```bash
POST /clickhouse/data/batch-insert
Content-Type: application/json

[
  {
    "eventTime": "2024-01-01T10:00:00",
    "userId": "user1",
    "eventType": "page_view",
    "pageUrl": "/home",
    "duration": 30,
    "deviceType": "mobile",
    "ipAddress": "192.168.1.1"
  }
]
```

#### 2. 批量插入（VALUES格式，性能最优）
```bash
POST /clickhouse/data/batch-insert-values
```

### 查询接口

#### 1. 查询用户行为日志
```bash
GET /clickhouse/query/logs?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00&limit=100
```

#### 2. 按事件类型统计
```bash
GET /clickhouse/query/stats/event-type?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

响应示例：
```json
{
  "success": true,
  "stats": [
    {
      "event_type": "page_view",
      "event_count": 1000,
      "unique_users": 500,
      "avg_duration": 25.5,
      "total_duration": 25500
    }
  ]
}
```

#### 3. 按小时统计
```bash
GET /clickhouse/query/stats/hourly?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

#### 4. 按设备类型统计
```bash
GET /clickhouse/query/stats/device?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

#### 5. 获取热门页面
```bash
GET /clickhouse/query/top-pages?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00&topN=10
```

#### 6. 分析用户行为路径
```bash
GET /clickhouse/query/user-path?userId=user1&startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

#### 7. 从物化视图查询（预聚合数据）
```bash
GET /clickhouse/query/materialized-view?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

### 监控接口

#### 1. 获取表统计信息
```bash
GET /clickhouse/monitor/table-stats
```

#### 2. 获取分区信息
```bash
GET /clickhouse/monitor/partitions
```

## 生产环境最佳实践

### 1. 表设计

#### 引擎选择
- **MergeTree**：最常用，适合大多数场景
- **ReplacingMergeTree**：自动去重
- **SummingMergeTree**：自动聚合
- **AggregatingMergeTree**：预聚合

#### 分区策略
```sql
-- 按月分区（推荐）
PARTITION BY toYYYYMM(event_time)

-- 按天分区（数据量大时）
PARTITION BY toYYYYMMDD(event_time)
```

#### 排序键设计
```sql
-- 按查询常用字段排序
ORDER BY (event_time, user_id)

-- 注意：排序键影响查询性能，需要根据实际查询场景设计
```

#### TTL 设置
```sql
-- 数据保留90天，自动清理
TTL event_time + INTERVAL 90 DAY
```

### 2. 批量插入优化

#### 批量大小建议
- **小批量**：1000-5000 条（实时写入）
- **中批量**：10000-50000 条（定时导入）
- **大批量**：100000+ 条（离线导入）

#### 插入方式对比

| 方式 | 性能 | 适用场景 |
|------|------|---------|
| **VALUES格式** | ⭐⭐⭐⭐⭐ | 大批量导入（推荐） |
| **JDBC批量** | ⭐⭐⭐⭐ | 中等批量 |
| **单条插入** | ⭐ | 不推荐 |

### 3. 查询优化

#### 使用分区裁剪
```sql
-- ✅ 好：使用分区字段
WHERE event_time >= '2024-01-01' AND event_time < '2024-02-01'

-- ❌ 差：不使用分区字段
WHERE user_id = 'user1'
```

#### 使用物化视图
```sql
-- 创建物化视图预聚合
CREATE MATERIALIZED VIEW user_behavior_hourly_mv
ENGINE = SummingMergeTree()
AS SELECT
    toStartOfHour(event_time) AS hour_time,
    event_type,
    count() AS event_count
FROM user_behavior_log
GROUP BY hour_time, event_type
```

#### 避免全表扫描
- ✅ 使用 WHERE 条件过滤
- ✅ 使用 LIMIT 限制结果集
- ✅ 使用合适的排序键

### 4. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # OLAP查询并发度较低
      connection-timeout: 60000  # 聚合查询可能较慢
      idle-timeout: 300000
```

### 5. 监控要点

- **分区大小**：监控各分区数据量
- **查询性能**：监控慢查询
- **磁盘使用**：监控数据增长
- **TTL执行**：监控数据清理情况

## 核心概念

### 1. OLAP vs OLTP

| 特性 | OLTP（MySQL） | OLAP（ClickHouse） |
|------|--------------|-------------------|
| **用途** | 事务处理 | 数据分析 |
| **写入** | 单条、高频 | 批量、低频 |
| **查询** | 简单查询 | 复杂聚合 |
| **更新** | 支持更新 | 主要追加 |
| **索引** | B+树 | 列式存储 |

### 2. 列式存储优势

- **压缩率高**：相同类型数据压缩效果好
- **查询快**：只读取需要的列
- **聚合快**：列式数据便于聚合计算

### 3. 分区（Partition）

- 按时间分区，提高查询效率
- 分区可以独立管理（删除、合并）
- 查询时自动分区裁剪

### 4. 物化视图（Materialized View）

- 预聚合常用查询
- 自动更新（基于源表）
- 显著提高查询性能

### 5. TTL（Time To Live）

- 自动清理过期数据
- 节省存储空间
- 支持多级TTL

## 典型使用场景

### 1. 用户行为分析

```sql
-- 统计各事件类型的用户数
SELECT 
    event_type,
    count() AS event_count,
    uniqExact(user_id) AS unique_users
FROM user_behavior_log
WHERE event_time >= today() - 7
GROUP BY event_type
```

### 2. 实时报表

```sql
-- 使用物化视图快速查询
SELECT 
    hour_time,
    sum(event_count) AS total_events
FROM user_behavior_hourly_mv
WHERE hour_time >= now() - INTERVAL 24 HOUR
GROUP BY hour_time
ORDER BY hour_time
```

### 3. 数据导入

```java
// 批量导入日志数据
List<UserBehaviorLog> logs = generateLogs(10000);
dataService.batchInsertWithValues(logs);
```

## 常见问题

### Q1: ClickHouse 适合存储业务数据吗？

**A**: 不适合。ClickHouse 是 OLAP 数据库，主要用于：
- 日志分析
- 数据仓库
- 实时报表
- 数据分析

业务数据应该存储在 MySQL/PostgreSQL 等 OLTP 数据库中。

### Q2: 如何选择批量插入的大小？

**A**: 
- 实时写入：1000-5000 条/批
- 定时导入：10000-50000 条/批
- 离线导入：100000+ 条/批

### Q3: 查询性能慢怎么办？

**A**: 
1. 检查是否使用了分区字段过滤
2. 检查排序键是否合理
3. 考虑使用物化视图预聚合
4. 检查索引使用情况

### Q4: 如何优化表结构？

**A**: 
1. 根据查询场景设计排序键
2. 合理设置分区策略
3. 使用合适的表引擎
4. 设置 TTL 管理数据生命周期

## 参考资源

- [ClickHouse 官方文档](https://clickhouse.com/docs/)
- [ClickHouse JDBC 驱动](https://github.com/ClickHouse/clickhouse-java)
- [ClickHouse 最佳实践](https://clickhouse.com/docs/en/guides/best-practices/)
