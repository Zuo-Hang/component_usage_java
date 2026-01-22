# Redis 模块快速测试指南

## ✅ 配置确认

你的配置已经正确：
- Redis 地址：`localhost:6379` ✅
- 端口：`6379` ✅
- 无密码：✅

配置文件位置：`src/main/resources/application.yml`

## 🚀 启动步骤

### 1. 确认 Redis 运行

```bash
# 检查 Redis 容器
docker ps | grep redis

# 测试 Redis 连接
docker exec <redis-container-name> redis-cli ping
# 应该返回: PONG
```

### 2. 启动应用

```bash
cd redis-example

# 方式1：Maven 启动（如果编译没问题）
mvn spring-boot:run

# 方式2：如果编译有问题，先打包
mvn clean package -DskipTests
java -jar target/redis-example-1.0.0.jar
```

应用启动后访问：`http://localhost:8080`

## 🧪 测试 API

### 快速测试（使用 curl）

```bash
# 1. Jedis - String 操作
curl http://localhost:8080/redis/client/jedis/string

# 2. Lettuce - String 操作  
curl http://localhost:8080/redis/client/lettuce/string

# 3. Redisson - Bucket 操作
curl http://localhost:8080/redis/client/redisson/bucket
```

### 浏览器测试

直接在浏览器中访问：

1. **Jedis 测试**：
   - http://localhost:8080/redis/client/jedis/string
   - http://localhost:8080/redis/client/jedis/hash
   - http://localhost:8080/redis/client/jedis/lock

2. **Lettuce 测试**：
   - http://localhost:8080/redis/client/lettuce/string
   - http://localhost:8080/redis/client/lettuce/async

3. **Redisson 测试**：
   - http://localhost:8080/redis/client/redisson/bucket
   - http://localhost:8080/redis/client/redisson/lock

## 🔍 验证数据

启动应用并调用 API 后，可以通过 Redis CLI 验证数据：

```bash
# 进入 Redis 容器
docker exec -it <redis-container-name> redis-cli

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

## 📋 完整测试清单

### Jedis 客户端
- [ ] `/redis/client/jedis/string` - String 操作
- [ ] `/redis/client/jedis/hash` - Hash 操作
- [ ] `/redis/client/jedis/list` - List 操作
- [ ] `/redis/client/jedis/set` - Set 操作
- [ ] `/redis/client/jedis/lock` - 分布式锁
- [ ] `/redis/client/jedis/pipeline` - 管道操作
- [ ] `/redis/client/jedis/transaction` - 事务操作

### Lettuce 客户端
- [ ] `/redis/client/lettuce/string` - 同步操作
- [ ] `/redis/client/lettuce/async` - 异步操作
- [ ] `/redis/client/lettuce/reactive` - 反应式操作
- [ ] `/redis/client/lettuce/lock` - 分布式锁

### Redisson 客户端
- [ ] `/redis/client/redisson/bucket` - Bucket 操作
- [ ] `/redis/client/redisson/map` - Map 操作
- [ ] `/redis/client/redisson/lock` - 分布式锁
- [ ] `/redis/client/redisson/fairlock` - 公平锁
- [ ] `/redis/client/redisson/semaphore` - 信号量
- [ ] `/redis/client/redisson/bloomfilter` - 布隆过滤器

## ⚠️ 如果编译失败

如果遇到编译错误（如 JDK 版本问题），可以：

1. **检查 JDK 版本**：
```bash
java -version
# 应该是 1.8 或更高版本
```

2. **使用 IDE 运行**：
   - 在 IDE（如 IntelliJ IDEA）中直接运行 `RedisExampleApplication`
   - IDE 会自动处理编译问题

3. **跳过编译直接运行**（如果已编译过）：
```bash
cd redis-example
java -cp "target/classes:$(mvn dependency:build-classpath -q -DincludeScope=runtime)" com.example.redis.RedisExampleApplication
```

## 📝 查看日志

应用启动后，查看控制台日志，应该看到：
- Spring Boot 启动信息
- Redis 连接信息
- API 调用日志

如果看到连接错误，检查：
1. Redis 是否在运行
2. 端口是否正确（6379）
3. 防火墙是否阻止连接
