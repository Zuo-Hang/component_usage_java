# Redis 工具类使用指南

## 概述

项目提供了三个 Redis 工具类，分别封装了 Jedis、Lettuce、Redisson 三种客户端的常见操作，提供统一简洁的 API。

## 工具类说明

### 1. JedisUtil - Jedis 工具类

**特点**：
- 线程不安全，使用连接池管理连接
- 每次操作自动获取和释放连接
- 支持管道和事务操作

**使用示例**：
```java
@Autowired
private JedisUtil jedisUtil;

// String 操作
jedisUtil.set("key", "value");
String value = jedisUtil.get("key");
jedisUtil.set("key", "value", 60); // 带过期时间

// Hash 操作
jedisUtil.hset("user:1001", "name", "张三");
String name = jedisUtil.hget("user:1001", "name");

// 分布式锁
boolean acquired = jedisUtil.tryLock("lock:key", "lock-value", 10);
if (acquired) {
    // 业务逻辑
    jedisUtil.releaseLock("lock:key", "lock-value");
}
```

### 2. LettuceUtil - Lettuce 工具类

**特点**：
- 线程安全，基于 Netty 的异步非阻塞 IO
- 支持同步和异步操作
- 连接可复用

**使用示例**：
```java
@Autowired
private LettuceUtil lettuceUtil;

// 同步操作
lettuceUtil.set("key", "value");
String value = lettuceUtil.get("key");

// 异步操作
lettuceUtil.setAsync("key", "value")
    .thenAccept(result -> {
        // 处理结果
    });

// 分布式锁
boolean acquired = lettuceUtil.tryLock("lock:key", "lock-value", 10);
```

### 3. RedissonUtil - Redisson 工具类

**特点**：
- 功能最强大，提供高级分布式对象
- 分布式锁支持可重入、自动续期
- 内置分页、布隆过滤器等高级功能

**使用示例**：
```java
@Autowired
private RedissonUtil redissonUtil;

// String 操作
redissonUtil.set("key", "value");
String value = redissonUtil.get("key");

// 分布式锁（推荐）
RLock lock = redissonUtil.getLock("lock:key");
lock.lock();
try {
    // 业务逻辑
} finally {
    lock.unlock();
}

// 或使用 tryLock
boolean acquired = redissonUtil.tryLock("lock:key", 10, 30, TimeUnit.SECONDS);

// 原子操作
long count = redissonUtil.increment("counter");
```

## API 对比

### String 操作

| 操作 | JedisUtil | LettuceUtil | RedissonUtil |
|------|-----------|-------------|--------------|
| 设置值 | `set(key, value)` | `set(key, value)` | `set(key, value)` |
| 获取值 | `get(key)` | `get(key)` | `get(key)` |
| 设置过期 | `set(key, value, seconds)` | `setex(key, seconds, value)` | `set(key, value, time, timeUnit)` |
| 递增 | `increment(key)` | `increment(key)` | `increment(key)` |

### Hash 操作

| 操作 | JedisUtil | LettuceUtil | RedissonUtil |
|------|-----------|-------------|--------------|
| 设置字段 | `hset(key, field, value)` | `hset(key, field, value)` | `hset(key, field, value)` |
| 获取字段 | `hget(key, field)` | `hget(key, field)` | `hget(key, field)` |
| 获取所有 | `hgetAll(key)` | `hgetAll(key)` | `hgetAll(key)` |

### 分布式锁

| 特性 | JedisUtil | LettuceUtil | RedissonUtil |
|------|-----------|-------------|--------------|
| 基本锁 | ✅ | ✅ | ✅ |
| 可重入 | ❌ | ❌ | ✅ |
| 自动续期 | ❌ | ❌ | ✅ |
| 公平锁 | ❌ | ❌ | ✅ |
| 读写锁 | ❌ | ❌ | ✅ |

## 使用建议

### 选择哪个工具类？

1. **JedisUtil**：
   - 适合简单场景
   - 需要手动管理连接
   - 性能较好

2. **LettuceUtil**：
   - 适合需要异步操作的场景
   - 线程安全
   - 适合高并发场景

3. **RedissonUtil**（推荐）：
   - 适合需要高级功能的场景
   - 分布式锁功能最强大
   - 代码最简洁

### 最佳实践

1. **统一使用一个工具类**：在同一个项目中，建议统一使用一种工具类，避免混用

2. **分布式锁推荐使用 RedissonUtil**：
   ```java
   // 推荐
   RLock lock = redissonUtil.getLock("lock:key");
   lock.lock();
   try {
       // 业务逻辑
   } finally {
       lock.unlock();
   }
   ```

3. **异步操作使用 LettuceUtil**：
   ```java
   // 适合高并发场景
   lettuceUtil.setAsync("key", "value")
       .thenAccept(result -> {
           // 处理结果
       });
   ```

## 测试 API

工具类提供了测试接口，可以通过以下方式测试：

```bash
# JedisUtil 测试
curl "http://localhost:8080/redis/util/jedis/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/jedis/get?key=test"

# LettuceUtil 测试
curl "http://localhost:8080/redis/util/lettuce/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/lettuce/get?key=test"

# RedissonUtil 测试
curl "http://localhost:8080/redis/util/redisson/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/redisson/get?key=test"

# 对比接口
curl "http://localhost:8080/redis/util/compare/set"
curl "http://localhost:8080/redis/util/compare/lock"
```

## 注意事项

1. **JedisUtil**：每次操作都会获取和释放连接，适合低并发场景
2. **LettuceUtil**：连接可复用，适合高并发场景
3. **RedissonUtil**：功能最全，但依赖较多，适合复杂场景

## 扩展

如果需要添加新的操作方法，可以在对应的工具类中添加：

```java
// 在 JedisUtil 中添加新方法
public void customMethod(String key) {
    try (Jedis jedis = getJedis()) {
        // 自定义操作
    }
}
```
