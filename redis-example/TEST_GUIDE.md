# Redis 模块测试指南

## 前提条件

确保 Redis 已经在 Docker 中启动并运行在 `localhost:6379`：

```bash
# 检查 Redis 是否运行
docker ps | grep redis

# 测试 Redis 连接
docker exec <redis-container-name> redis-cli ping
# 应该返回: PONG
```

## 启动应用

### 方法1：使用 Maven（推荐）

```bash
cd redis-example
mvn spring-boot:run
```

### 方法2：打包后运行

```bash
cd redis-example
mvn clean package -DskipTests
java -jar target/redis-example-1.0.0.jar
```

应用启动后，默认运行在 `http://localhost:8080`

## 测试 API

### 1. Jedis 客户端测试

```bash
# String 操作
curl http://localhost:8080/redis/client/jedis/string

# Hash 操作
curl http://localhost:8080/redis/client/jedis/hash

# List 操作
curl http://localhost:8080/redis/client/jedis/list

# Set 操作
curl http://localhost:8080/redis/client/jedis/set

# 分布式锁
curl http://localhost:8080/redis/client/jedis/lock

# 管道操作
curl http://localhost:8080/redis/client/jedis/pipeline

# 事务操作
curl http://localhost:8080/redis/client/jedis/transaction
```

### 2. Lettuce 客户端测试

```bash
# 同步 String 操作
curl http://localhost:8080/redis/client/lettuce/string

# 同步 Hash 操作
curl http://localhost:8080/redis/client/lettuce/hash

# 同步 List 操作
curl http://localhost:8080/redis/client/lettuce/list

# 同步 Set 操作
curl http://localhost:8080/redis/client/lettuce/set

# 异步操作
curl http://localhost:8080/redis/client/lettuce/async

# 反应式操作
curl http://localhost:8080/redis/client/lettuce/reactive

# 分布式锁
curl http://localhost:8080/redis/client/lettuce/lock

# 批量操作
curl http://localhost:8080/redis/client/lettuce/batch
```

### 3. Redisson 客户端测试

```bash
# Bucket 操作
curl http://localhost:8080/redis/client/redisson/bucket

# Map 操作
curl http://localhost:8080/redis/client/redisson/map

# List 操作
curl http://localhost:8080/redis/client/redisson/list

# Set 操作
curl http://localhost:8080/redis/client/redisson/set

# 分布式锁
curl http://localhost:8080/redis/client/redisson/lock

# 公平锁
curl http://localhost:8080/redis/client/redisson/fairlock

# 读写锁
curl http://localhost:8080/redis/client/redisson/readwritelock

# 信号量
curl http://localhost:8080/redis/client/redisson/semaphore

# 布隆过滤器
curl http://localhost:8080/redis/client/redisson/bloomfilter

# 原子操作
curl http://localhost:8080/redis/client/redisson/atomic
```

### 4. 框架对比

```bash
# String 操作对比
curl http://localhost:8080/redis/client/compare/string

# 分布式锁对比
curl http://localhost:8080/redis/client/compare/lock
```

## 查看日志

应用运行时会输出详细的日志，包括：
- Redis 操作结果
- 连接状态
- 错误信息（如果有）

## 验证数据

可以通过 Redis CLI 验证数据是否写入：

```bash
# 进入 Redis 容器
docker exec -it <redis-container-name> redis-cli

# 查看所有键
KEYS *

# 查看特定键的值
GET jedis:user:1001:name
GET lettuce:user:1001:name
GET user:1001:name

# 查看 Hash
HGETALL jedis:user:1001
HGETALL lettuce:user:1001
HGETALL user:1001

# 查看 List
LRANGE jedis:task:queue 0 -1
LRANGE lettuce:task:queue 0 -1

# 查看 Set
SMEMBERS jedis:tags:article:1001
SMEMBERS lettuce:tags:article:1001
```

## 常见问题

### 1. 连接失败

**错误**: `Unable to connect to Redis`

**解决**:
- 确认 Redis 容器正在运行：`docker ps | grep redis`
- 确认端口映射正确：`docker port <redis-container-name>`
- 检查 `application.yml` 中的配置是否正确

### 2. 端口被占用

**错误**: `Port 8080 is already in use`

**解决**:
- 修改 `application.yml` 中的 `server.port`
- 或停止占用端口的进程

### 3. 依赖问题

**错误**: `ClassNotFoundException` 或 `NoClassDefFoundError`

**解决**:
```bash
cd redis-example
mvn clean install -DskipTests
```

## 快速测试脚本

创建一个测试脚本 `test-redis.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/redis/client"

echo "=== 测试 Jedis ==="
curl -s "$BASE_URL/jedis/string"
echo ""

echo "=== 测试 Lettuce ==="
curl -s "$BASE_URL/lettuce/string"
echo ""

echo "=== 测试 Redisson ==="
curl -s "$BASE_URL/redisson/bucket"
echo ""

echo "=== 测试完成 ==="
```

运行：
```bash
chmod +x test-redis.sh
./test-redis.sh
```
