# Redis 模块测试成功指南

## ✅ 编译问题已解决

通过升级 Lombok 版本到 `1.18.30`，编译问题已解决。

## 🚀 启动应用

```bash
cd redis-example
mvn spring-boot:run
```

应用启动后，默认运行在：`http://localhost:8080`

## 🧪 快速测试

### 1. 浏览器测试

直接在浏览器中访问以下 URL：

**Jedis 客户端**：
- http://localhost:8080/redis/client/jedis/string
- http://localhost:8080/redis/client/jedis/hash
- http://localhost:8080/redis/client/jedis/lock

**Lettuce 客户端**：
- http://localhost:8080/redis/client/lettuce/string
- http://localhost:8080/redis/client/lettuce/async

**Redisson 客户端**：
- http://localhost:8080/redis/client/redisson/bucket
- http://localhost:8080/redis/client/redisson/lock

### 2. 使用 curl 测试

```bash
# 测试 Jedis
curl http://localhost:8080/redis/client/jedis/string

# 测试 Lettuce
curl http://localhost:8080/redis/client/lettuce/string

# 测试 Redisson
curl http://localhost:8080/redis/client/redisson/bucket
```

### 3. 验证 Redis 数据

调用 API 后，验证数据是否写入 Redis：

```bash
# 进入 Redis 容器
docker exec -it redis_test redis-cli

# 查看所有键
KEYS *

# 查看 Jedis 写入的数据
GET jedis:user:1001:name
HGETALL jedis:user:1001

# 查看 Lettuce 写入的数据
GET lettuce:user:1001:name
HGETALL lettuce:user:1001

# 查看 Redisson 写入的数据
GET user:1001:name
HGETALL user:1001
```

## 📋 所有可用 API

### Jedis (7个)
- `/redis/client/jedis/string` - String 操作
- `/redis/client/jedis/hash` - Hash 操作
- `/redis/client/jedis/list` - List 操作
- `/redis/client/jedis/set` - Set 操作
- `/redis/client/jedis/lock` - 分布式锁
- `/redis/client/jedis/pipeline` - 管道操作
- `/redis/client/jedis/transaction` - 事务操作

### Lettuce (8个)
- `/redis/client/lettuce/string` - 同步 String 操作
- `/redis/client/lettuce/hash` - 同步 Hash 操作
- `/redis/client/lettuce/list` - 同步 List 操作
- `/redis/client/lettuce/set` - 同步 Set 操作
- `/redis/client/lettuce/async` - 异步操作
- `/redis/client/lettuce/reactive` - 反应式操作
- `/redis/client/lettuce/lock` - 分布式锁
- `/redis/client/lettuce/batch` - 批量操作

### Redisson (10个)
- `/redis/client/redisson/bucket` - Bucket 操作
- `/redis/client/redisson/map` - Map 操作
- `/redis/client/redisson/list` - List 操作
- `/redis/client/redisson/set` - Set 操作
- `/redis/client/redisson/lock` - 分布式锁
- `/redis/client/redisson/fairlock` - 公平锁
- `/redis/client/redisson/readwritelock` - 读写锁
- `/redis/client/redisson/semaphore` - 信号量
- `/redis/client/redisson/bloomfilter` - 布隆过滤器
- `/redis/client/redisson/atomic` - 原子操作

### 对比接口 (2个)
- `/redis/client/compare/string` - String 操作对比
- `/redis/client/compare/lock` - 分布式锁对比

## 🎯 测试流程

1. **启动应用**：`mvn spring-boot:run`
2. **等待启动完成**：看到 "Started RedisExampleApplication"
3. **调用 API**：访问任意一个测试接口
4. **查看日志**：控制台会显示详细的操作日志
5. **验证数据**：通过 Redis CLI 查看写入的数据

## 📝 查看日志

应用运行时会输出详细日志：
- Redis 连接信息
- 每个操作的结果
- 错误信息（如果有）

如果看到连接错误，检查：
1. Redis 容器是否运行：`docker ps | grep redis`
2. 端口是否正确：`localhost:6379`
3. 防火墙是否阻止连接

## ✅ 成功标志

如果看到以下情况，说明测试成功：
- 应用正常启动（无错误）
- API 返回成功消息
- Redis 中有数据写入
- 日志显示操作成功
