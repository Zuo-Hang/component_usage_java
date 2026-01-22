# Redis 重复设置 Key 的行为说明

## 📋 概述

当对 Redis 中已存在的 key 重复设置值时，不同数据类型的表现是不同的。本文档详细说明各种情况下的行为。

## 🔍 测试接口

可以通过以下接口查看实际演示：

```bash
curl "http://localhost:8080/redis/util/demo/overwrite"
```

## 📊 不同数据类型的行为

### 1. String 类型：**完全覆盖**

**行为**：`SET` 命令会直接替换整个值，旧值被完全覆盖。

**示例**：
```java
jedisUtil.set("key", "原始值");      // key = "原始值"
jedisUtil.set("key", "新值");        // key = "新值"（旧值被完全替换）
```

**特点**：
- ✅ 旧值被完全替换
- ✅ 操作是原子的
- ⚠️ 如果旧值很大，新值很小，内存会立即释放

---

### 2. Hash 类型：**部分更新**

**行为**：`HSET` 只更新指定的字段，其他字段保持不变。

**示例**：
```java
// 第一次设置
jedisUtil.hset("user:1001", "name", "张三");
jedisUtil.hset("user:1001", "age", "25");
jedisUtil.hset("user:1001", "email", "zhangsan@example.com");
// 结果：{name=张三, age=25, email=zhangsan@example.com}

// 只更新 name 字段
jedisUtil.hset("user:1001", "name", "李四");
// 结果：{name=李四, age=25, email=zhangsan@example.com}
// age 和 email 保持不变
```

**特点**：
- ✅ 只更新指定字段
- ✅ 其他字段不受影响
- ✅ 适合部分更新场景

---

### 3. List 类型：**追加 vs 覆盖**

**行为**：
- `LPUSH`/`RPUSH`：追加元素，不会覆盖
- `SET` 命令：会覆盖整个 List（将 List 类型转换为 String）

**示例**：
```java
// 第一次 LPUSH
jedisUtil.lpush("list:test", "元素1", "元素2", "元素3");
// 结果：[元素3, 元素2, 元素1]

// 再次 LPUSH（追加）
jedisUtil.lpush("list:test", "新元素");
// 结果：[新元素, 元素3, 元素2, 元素1]（追加，不覆盖）

// ⚠️ 如果使用 SET 命令会覆盖整个 List
jedisUtil.set("list:test", "字符串值");
// 结果：List 被转换为 String，所有元素丢失
```

**特点**：
- ✅ `LPUSH`/`RPUSH` 追加元素
- ⚠️ `SET` 会覆盖整个 List（类型转换）
- ⚠️ 注意不要混用 `SET` 和 List 操作

---

### 4. Set 类型：**添加元素**

**行为**：`SADD` 添加新元素，已存在的元素不会重复，不会覆盖整个 Set。

**示例**：
```java
// 第一次 SADD
jedisUtil.sadd("set:test", "Java", "Redis", "Spring");
// 结果：{Java, Redis, Spring}

// 再次 SADD（添加）
jedisUtil.sadd("set:test", "MyBatis", "Java");
// 结果：{Java, Redis, Spring, MyBatis}
// Java 已存在，不会重复添加
```

**特点**：
- ✅ 添加新元素
- ✅ 自动去重（已存在元素不会重复）
- ✅ 不会覆盖整个 Set

---

## ⚠️ 重要注意事项

### 1. 类型转换问题

**危险操作**：
```java
// 先设置 String
jedisUtil.set("key", "字符串值");

// 然后尝试 List 操作（会报错或产生意外结果）
jedisUtil.lpush("key", "元素1");  // ❌ 错误！
```

**正确做法**：
- 确保 key 的类型一致
- 使用前先检查 key 的类型：`TYPE key`
- 或者使用不同的 key 名称

### 2. 内存管理

- Redis **不会自动删除**旧内容
- `SET` 操作会**立即替换**整个值，旧值的内存会被释放
- Hash 的部分更新不会释放其他字段的内存（除非字段被删除）

### 3. 原子性

- 所有操作都是**原子的**
- 不会出现部分更新的情况
- 但要注意操作的顺序和逻辑

## 📝 最佳实践

### 1. 明确数据类型

```java
// ✅ 好的做法：使用前缀区分类型
jedisUtil.set("str:user:1001:name", "张三");
jedisUtil.hset("hash:user:1001", "name", "张三");
jedisUtil.lpush("list:task:queue", "任务1");
```

### 2. 检查 key 是否存在

```java
if (jedisUtil.exists("key")) {
    // key 已存在，决定是覆盖还是更新
    String oldValue = jedisUtil.get("key");
    // 根据业务逻辑处理
}
```

### 3. 使用合适的命令

```java
// ✅ 部分更新 Hash
jedisUtil.hset("user:1001", "name", "新名字");  // 只更新 name

// ✅ 追加 List
jedisUtil.lpush("queue", "新任务");  // 追加，不覆盖

// ✅ 完全替换 String
jedisUtil.set("cache:key", "新值");  // 完全替换
```

## 🔗 相关文档

- [Redis 官方文档 - SET 命令](https://redis.io/commands/set/)
- [Redis 官方文档 - HSET 命令](https://redis.io/commands/hset/)
- [Redis 官方文档 - LPUSH 命令](https://redis.io/commands/lpush/)
- [Redis 官方文档 - SADD 命令](https://redis.io/commands/sadd/)

## 🧪 测试命令

```bash
# 查看演示
curl "http://localhost:8080/redis/util/demo/overwrite"

# 测试 String 覆盖
curl "http://localhost:8080/redis/util/jedis/set?key=test:string&value=第一次"
curl "http://localhost:8080/redis/util/jedis/set?key=test:string&value=第二次"
curl "http://localhost:8080/redis/util/jedis/get?key=test:string"

# 测试 Hash 部分更新
curl "http://localhost:8080/redis/util/jedis/hset?key=test:hash&field=name&value=张三"
curl "http://localhost:8080/redis/util/jedis/hset?key=test:hash&field=age&value=25"
curl "http://localhost:8080/redis/util/jedis/hset?key=test:hash&field=name&value=李四"
curl "http://localhost:8080/redis/util/jedis/hget?key=test:hash&field=name"
```
