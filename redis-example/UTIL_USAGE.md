# Redis 工具类使用指南

## 📦 工具类概述

项目提供了三个 Redis 工具类，封装了常见的 Redis 操作：

1. **JedisUtil** - Jedis 客户端工具类
2. **LettuceUtil** - Lettuce 客户端工具类  
3. **RedissonUtil** - Redisson 客户端工具类

## 🚀 快速开始

### 1. 注入工具类

```java
@Autowired
private JedisUtil jedisUtil;

@Autowired
private LettuceUtil lettuceUtil;

@Autowired
private RedissonUtil redissonUtil;
```

### 2. 使用工具类

```java
// String 操作
jedisUtil.set("key", "value");
String value = jedisUtil.get("key");

// Hash 操作
jedisUtil.hset("user:1001", "name", "张三");
String name = jedisUtil.hget("user:1001", "name");

// 分布式锁
boolean acquired = jedisUtil.tryLock("lock:key", "value", 10);
```

## 📋 API 列表

### JedisUtil

#### String 操作
- `set(key, value)` - 设置值
- `set(key, value, expireSeconds)` - 设置值并指定过期时间
- `get(key)` - 获取值
- `delete(key)` - 删除键
- `exists(key)` - 判断键是否存在
- `expire(key, seconds)` - 设置过期时间
- `increment(key)` - 递增
- `incrementBy(key, increment)` - 递增指定值
- `decrement(key)` - 递减

#### Hash 操作
- `hset(key, field, value)` - 设置 Hash 字段
- `hget(key, field)` - 获取 Hash 字段值
- `hgetAll(key)` - 获取所有 Hash 字段和值
- `hdel(key, fields...)` - 删除 Hash 字段
- `hexists(key, field)` - 判断 Hash 字段是否存在
- `hkeys(key)` - 获取 Hash 所有字段
- `hvals(key)` - 获取 Hash 所有值

#### List 操作
- `lpush(key, values...)` - 从左侧推入
- `rpush(key, values...)` - 从右侧推入
- `lpop(key)` - 从左侧弹出
- `rpop(key)` - 从右侧弹出
- `llen(key)` - 获取 List 长度
- `lrange(key, start, end)` - 获取指定范围元素
- `lrangeAll(key)` - 获取所有元素

#### Set 操作
- `sadd(key, members...)` - 添加元素
- `srem(key, members...)` - 移除元素
- `sismember(key, member)` - 判断元素是否存在
- `smembers(key)` - 获取所有成员
- `scard(key)` - 获取 Set 大小

#### 分布式锁
- `tryLock(lockKey, lockValue, expireSeconds)` - 尝试获取锁
- `releaseLock(lockKey, lockValue)` - 释放锁

#### 批量操作
- `batchSet(keyValues)` - 批量设置
- `batchGet(keys...)` - 批量获取

### LettuceUtil

#### 同步操作（与 JedisUtil 类似）
- String、Hash、List、Set 操作与 JedisUtil 基本相同
- `setex(key, seconds, value)` - 设置值并指定过期时间

#### 异步操作
- `setAsync(key, value)` - 异步设置值
- `getAsync(key)` - 异步获取值
- `deleteAsync(key)` - 异步删除键

### RedissonUtil

#### 基本操作（与 JedisUtil 类似）
- String、Hash、List、Set 操作基本相同
- `set(key, value, time, timeUnit)` - 设置值并指定过期时间

#### 高级功能
- `getLock(lockKey)` - 获取分布式锁（RLock）
- `tryLock(lockKey, waitTime, leaseTime, timeUnit)` - 尝试获取锁
- `unlock(lockKey)` - 释放锁
- `getFairLock(lockKey)` - 获取公平锁
- `getReadWriteLock(lockKey)` - 获取读写锁
- `getAtomicLong(key)` - 获取原子长整型
- `increment(key)` - 原子递增
- `getBloomFilter(key)` - 获取布隆过滤器
- `getSemaphore(key)` - 获取信号量
- `getQueue(key)` - 获取队列

## 🧪 测试工具类

### 通过 API 测试

```bash
# JedisUtil
curl "http://localhost:8080/redis/util/jedis/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/jedis/get?key=test"

# LettuceUtil
curl "http://localhost:8080/redis/util/lettuce/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/lettuce/get?key=test"

# RedissonUtil
curl "http://localhost:8080/redis/util/redisson/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/redisson/get?key=test"

# 对比接口
curl "http://localhost:8080/redis/util/compare/set"
curl "http://localhost:8080/redis/util/compare/lock"
```

## 💡 使用示例

### 示例1：缓存用户信息

```java
@Autowired
private JedisUtil jedisUtil;

public void cacheUser(Long userId, String name, String email) {
    String key = "user:" + userId;
    jedisUtil.hset(key, "name", name);
    jedisUtil.hset(key, "email", email);
    jedisUtil.expire(key, 3600); // 1小时过期
}

public User getUser(Long userId) {
    String key = "user:" + userId;
    Map<String, String> userMap = jedisUtil.hgetAll(key);
    // 转换为 User 对象
    return convertToUser(userMap);
}
```

### 示例2：分布式锁

```java
@Autowired
private RedissonUtil redissonUtil;

public void processOrder(Long orderId) {
    String lockKey = "lock:order:" + orderId;
    RLock lock = redissonUtil.getLock(lockKey);
    
    try {
        if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
            // 处理订单
            // ...
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

### 示例3：计数器

```java
@Autowired
private RedissonUtil redissonUtil;

public long incrementViewCount(Long articleId) {
    String key = "article:view:" + articleId;
    return redissonUtil.increment(key);
}
```

## ⚖️ 工具类对比

| 特性 | JedisUtil | LettuceUtil | RedissonUtil |
|------|-----------|-------------|--------------|
| 线程安全 | ❌ | ✅ | ✅ |
| 异步支持 | ❌ | ✅ | ✅ |
| 分布式锁 | 基础 | 基础 | 高级（可重入、自动续期） |
| 代码简洁度 | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| 功能丰富度 | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 推荐场景 | 简单场景 | 高并发场景 | 复杂场景 |

## 🎯 选择建议

- **简单 CRUD 操作**：三种工具类都可以，API 基本一致
- **高并发场景**：推荐 LettuceUtil（异步支持）
- **分布式锁**：强烈推荐 RedissonUtil（功能最强大）
- **复杂数据结构**：推荐 RedissonUtil（支持更多高级数据结构）

## 📝 注意事项

1. **JedisUtil**：每次操作都会获取和释放连接，适合低并发场景
2. **LettuceUtil**：连接可复用，适合高并发场景，支持异步操作
3. **RedissonUtil**：功能最全，但依赖较多，适合复杂场景

## 🔗 相关文件

- `JedisUtil.java` - Jedis 工具类源码
- `LettuceUtil.java` - Lettuce 工具类源码
- `RedissonUtil.java` - Redisson 工具类源码
- `RedisUtilController.java` - 工具类测试控制器
