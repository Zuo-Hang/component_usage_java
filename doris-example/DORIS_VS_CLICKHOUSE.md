# Doris vs ClickHouse 详细对比

本文档详细对比 Apache Doris 和 ClickHouse 在生产环境中的使用差异。

## 一、核心差异总结

| 维度 | ClickHouse | Doris | 说明 |
|------|-----------|-------|------|
| **SQL兼容性** | 非标准SQL | ✅ MySQL兼容 | Doris更容易上手和迁移 |
| **实时更新** | 主要追加 | ✅ UPDATE/DELETE | Doris适合需要更新的场景 |
| **多表JOIN** | 性能一般 | ✅ CBO优化，性能优异 | Doris适合复杂关联分析 |
| **并发性能** | 中等 | ✅ 高并发支持 | Doris支持更高并发 |
| **物化视图** | 单表 | ✅ 多表物化视图 | Doris更灵活 |
| **查询重写** | 不支持 | ✅ 自动查询重写 | Doris更智能 |
| **协议** | HTTP/原生 | ✅ MySQL协议 | Doris兼容MySQL工具 |

## 二、使用场景对比

### 1. 日志分析场景

**ClickHouse** ✅
- 单表查询性能优异
- 列式存储压缩率高
- 适合时序数据

**Doris** ✅
- 性能良好
- MySQL协议，易于使用

**结论**：两者都适合，ClickHouse 在单表场景下可能略优

### 2. 实时更新场景

**ClickHouse** ❌
- 主要支持追加
- 更新需要重建表或使用 FINAL（性能差）

**Doris** ✅
- 支持 UPDATE/DELETE
- Unique 模型 + Merge-On-Write 性能好
- 适合订单状态更新等场景

**结论**：**Doris 明显优势**

### 3. 多表JOIN场景

**ClickHouse** ⚠️
- JOIN 性能一般
- 缺乏 CBO 优化
- 复杂 JOIN 性能较差

**Doris** ✅
- CBO 优化器自动优化
- 复杂 JOIN 性能优异
- 适合星型模型、雪花模型

**结论**：**Doris 明显优势**

### 4. 高并发场景

**ClickHouse** ⚠️
- 并发性能中等
- 连接池建议较小（20-30）

**Doris** ✅
- 支持高并发查询
- 连接池可以设置较大（50-100）

**结论**：**Doris 优势**

### 5. MySQL迁移场景

**ClickHouse** ❌
- SQL语法差异大
- 迁移成本高

**Doris** ✅
- MySQL协议兼容
- SQL语法基本一致
- 迁移成本低

**结论**：**Doris 明显优势**

## 三、技术实现对比

### 1. 表设计

#### ClickHouse
```sql
CREATE TABLE user_behavior_log (
    event_time DateTime,
    user_id String,
    ...
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_time)
ORDER BY (event_time, user_id)
TTL event_time + INTERVAL 90 DAY
```

#### Doris
```sql
-- Unique模型（支持更新）
CREATE TABLE orders (
    order_id BIGINT NOT NULL,
    status VARCHAR(20),
    ...
) ENGINE=OLAP
UNIQUE KEY(order_id)
PARTITION BY RANGE(order_time) (...)
DISTRIBUTED BY HASH(order_id) BUCKETS 10
PROPERTIES (
    "enable_unique_key_merge_on_write" = "true"
)
```

**差异**：
- ClickHouse：单一 MergeTree 引擎
- Doris：多种表模型（Unique/Duplicate/Aggregate）

### 2. 数据更新

#### ClickHouse
```java
// ❌ 不支持直接UPDATE
// 需要重建表或使用FINAL查询（性能差）
```

#### Doris
```java
// ✅ 支持直接UPDATE
jdbcTemplate.update("UPDATE orders SET status = ? WHERE order_id = ?", 
                    "shipped", orderId);
```

**差异**：
- ClickHouse：主要追加，更新困难
- Doris：支持实时更新，性能好

### 3. 多表JOIN

#### ClickHouse
```sql
-- ⚠️ JOIN性能一般，缺乏CBO优化
SELECT u.*, o.*
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
```

#### Doris
```sql
-- ✅ CBO自动优化，性能优异
SELECT u.*, o.*
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
-- CBO会自动选择最优执行计划
```

**差异**：
- ClickHouse：手动优化，性能一般
- Doris：CBO自动优化，性能优异

### 4. 物化视图

#### ClickHouse
```sql
-- ⚠️ 仅支持单表物化视图
CREATE MATERIALIZED VIEW user_hourly_mv
AS SELECT
    toStartOfHour(event_time) AS hour_time,
    count() AS event_count
FROM user_behavior_log
GROUP BY hour_time
```

#### Doris
```sql
-- ✅ 支持多表物化视图
CREATE MATERIALIZED VIEW user_order_mv
AS SELECT
    u.user_id,
    COUNT(o.order_id) AS order_count
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
GROUP BY u.user_id
```

**差异**：
- ClickHouse：单表物化视图
- Doris：多表物化视图 + 自动查询重写

## 四、性能对比

### 1. 单表查询

| 场景 | ClickHouse | Doris |
|------|-----------|-------|
| **单表聚合** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **单表过滤** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **单表排序** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

**结论**：ClickHouse 在单表场景下略优

### 2. 多表JOIN

| 场景 | ClickHouse | Doris |
|------|-----------|-------|
| **两表JOIN** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **三表JOIN** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **复杂JOIN** | ⭐ | ⭐⭐⭐⭐ |

**结论**：Doris 在多表JOIN场景下明显优势

### 3. 实时更新

| 场景 | ClickHouse | Doris |
|------|-----------|-------|
| **批量追加** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **单条更新** | ❌ | ⭐⭐⭐⭐ |
| **批量更新** | ❌ | ⭐⭐⭐⭐ |

**结论**：Doris 在更新场景下明显优势

### 4. 并发性能

| 场景 | ClickHouse | Doris |
|------|-----------|-------|
| **低并发（<10）** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **中并发（10-50）** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **高并发（>50）** | ⭐⭐ | ⭐⭐⭐⭐ |

**结论**：Doris 在高并发场景下优势

## 五、选择建议

### 选择 ClickHouse 的场景

1. ✅ **单表分析为主**
   - 日志分析
   - 时序数据分析
   - 单表聚合查询

2. ✅ **不需要更新**
   - 只追加数据
   - 不需要修改历史数据

3. ✅ **对单表性能要求极高**
   - 单表查询性能要求最高

### 选择 Doris 的场景

1. ✅ **需要实时更新**
   - 订单状态更新
   - 用户信息更新
   - 需要 DELETE 操作

2. ✅ **多表JOIN频繁**
   - 复杂关联分析
   - 星型模型查询
   - 多维度分析

3. ✅ **高并发查询**
   - 大量并发用户
   - 实时报表系统

4. ✅ **MySQL迁移**
   - 从MySQL迁移
   - 需要兼容MySQL工具

5. ✅ **实时数仓**
   - 需要更新和查询并存
   - 复杂业务场景

## 六、实际案例对比

### 案例1：订单分析系统

**需求**：
- 存储订单数据
- 需要更新订单状态
- 多表关联查询（用户+订单+产品）

**ClickHouse**：
- ❌ 更新困难
- ⚠️ 多表JOIN性能一般

**Doris**：
- ✅ 支持实时更新
- ✅ 多表JOIN性能优异

**结论**：**选择 Doris**

### 案例2：日志分析系统

**需求**：
- 存储日志数据
- 单表聚合查询
- 不需要更新

**ClickHouse**：
- ✅ 单表性能优异
- ✅ 压缩率高

**Doris**：
- ✅ 性能良好
- ✅ MySQL协议易用

**结论**：**两者都适合，ClickHouse 可能略优**

### 案例3：实时数仓

**需求**：
- 多数据源整合
- 复杂JOIN查询
- 需要实时更新
- 高并发查询

**ClickHouse**：
- ❌ 更新困难
- ⚠️ JOIN性能一般
- ⚠️ 并发性能中等

**Doris**：
- ✅ 支持实时更新
- ✅ JOIN性能优异
- ✅ 高并发支持

**结论**：**选择 Doris**

## 七、迁移建议

### 从 ClickHouse 迁移到 Doris

**优势**：
- ✅ 获得实时更新能力
- ✅ 提高多表JOIN性能
- ✅ 提高并发性能

**注意事项**：
- SQL语法需要调整（MySQL兼容）
- 表结构需要重新设计
- 数据需要重新导入

### 从 MySQL 迁移到 Doris

**优势**：
- ✅ SQL语法基本兼容
- ✅ 使用相同JDBC驱动
- ✅ 迁移成本低

**注意事项**：
- 表结构需要优化（分区、分桶）
- 选择合适的表模型
- 数据需要批量导入

## 八、总结

### ClickHouse 优势
- 单表查询性能优异
- 列式存储压缩率高
- 适合日志和时序数据

### Doris 优势
- MySQL协议兼容
- 实时更新支持
- 多表JOIN性能优异
- 高并发支持
- 多表物化视图

### 选择原则
- **单表分析、不需要更新** → ClickHouse
- **需要更新、多表JOIN、高并发** → Doris
- **MySQL迁移** → Doris
