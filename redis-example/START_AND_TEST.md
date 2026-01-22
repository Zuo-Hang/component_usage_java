# Redis 模块启动和测试

## ✅ 配置确认

你的配置已经正确：
- **Redis 地址**: `localhost:6379` ✅
- **Redis 容器**: `redis_test` 正在运行 ✅
- **端口映射**: `0.0.0.0:6379->6379/tcp` ✅

## 🚀 启动应用

### 方式1：使用 IDE（推荐，避免编译问题）

1. 在 IDE（IntelliJ IDEA / Eclipse）中打开项目
2. 找到 `RedisExampleApplication.java`
3. 右键 → Run
4. 等待应用启动（看到 "Started RedisExampleApplication"）

### 方式2：使用 Maven（如果编译成功）

```bash
cd redis-example
mvn spring-boot:run
```

### 方式3：打包后运行

```bash
cd redis-example
mvn clean package -DskipTests
java -jar target/redis-example-1.0.0.jar
```

**注意**：如果遇到编译错误（JDK 版本问题），建议使用 IDE 运行。

## 🧪 测试 API

应用启动后（默认端口 8080），可以通过以下方式测试：

### 1. 浏览器测试（最简单）

直接在浏览器中访问：

**Jedis 测试**：
- http://localhost:8080/redis/client/jedis/string
- http://localhost:8080/redis/client/jedis/hash
- http://localhost:8080/redis/client/jedis/lock

**Lettuce 测试**：
- http://localhost:8080/redis/client/lettuce/string
- http://localhost:8080/redis/client/lettuce/async

**Redisson 测试**：
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

### 3. 使用 Postman 或 HTTP 客户端

导入以下请求：

```
GET http://localhost:8080/redis/client/jedis/string
GET http://localhost:8080/redis/client/lettuce/string
GET http://localhost:8080/redis/client/redisson/bucket
```

## 🔍 验证数据写入 Redis

调用 API 后，可以通过 Redis CLI 验证数据：

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

## 📋 完整测试清单

### Jedis 客户端（7个接口）
```
GET /redis/client/jedis/string
GET /redis/client/jedis/hash
GET /redis/client/jedis/list
GET /redis/client/jedis/set
GET /redis/client/jedis/lock
GET /redis/client/jedis/pipeline
GET /redis/client/jedis/transaction
```

### Lettuce 客户端（8个接口）
```
GET /redis/client/lettuce/string
GET /redis/client/lettuce/hash
GET /redis/client/lettuce/list
GET /redis/client/lettuce/set
GET /redis/client/lettuce/async
GET /redis/client/lettuce/reactive
GET /redis/client/lettuce/lock
GET /redis/client/lettuce/batch
```

### Redisson 客户端（10个接口）
```
GET /redis/client/redisson/bucket
GET /redis/client/redisson/map
GET /redis/client/redisson/list
GET /redis/client/redisson/set
GET /redis/client/redisson/lock
GET /redis/client/redisson/fairlock
GET /redis/client/redisson/readwritelock
GET /redis/client/redisson/semaphore
GET /redis/client/redisson/bloomfilter
GET /redis/client/redisson/atomic
```

### 对比接口（2个）
```
GET /redis/client/compare/string
GET /redis/client/compare/lock
```

## ⚠️ 常见问题

### 1. 应用启动失败

**检查**：
- Redis 是否运行：`docker ps | grep redis`
- 端口是否正确：`docker port redis_test`
- 日志中的错误信息

### 2. 连接 Redis 失败

**错误信息**：`Unable to connect to Redis`

**解决**：
```bash
# 确认 Redis 运行
docker ps | grep redis

# 测试连接
docker exec redis_test redis-cli ping
# 应该返回: PONG
```

### 3. 端口 8080 被占用

**解决**：修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

## 📝 查看日志

应用运行时会输出详细日志：
- Spring Boot 启动信息
- Redis 连接信息
- 每个 API 调用的操作结果

如果看到错误，检查日志中的具体错误信息。

## 🎯 快速验证

最简单的验证方式：

1. **启动应用**（IDE 或 Maven）
2. **访问一个简单的 API**：
   ```
   http://localhost:8080/redis/client/jedis/string
   ```
3. **查看返回结果**：应该看到 "Jedis String操作示例执行完成，请查看日志"
4. **验证 Redis 数据**：
   ```bash
   docker exec redis_test redis-cli GET jedis:user:1001:name
   # 应该返回: 张三
   ```

如果以上步骤都成功，说明 Redis 模块工作正常！🎉
