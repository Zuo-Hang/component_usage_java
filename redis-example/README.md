# Redis 使用示例模块

本模块演示了三种 Redis 客户端（Jedis、Lettuce、Redisson）的使用方式，并提供了封装好的工具类。

## 功能特性

### 三种客户端对比

| 特性 | Jedis | Lettuce | Redisson |
|------|-------|---------|----------|
| 线程安全 | ❌ | ✅ | ✅ |
| 异步支持 | ❌ | ✅ | ✅ |
| 分布式锁 | 基础 | 基础 | 高级（可重入、自动续期） |
| 代码简洁度 | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| 功能丰富度 | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 推荐场景 | 简单场景 | 高并发场景 | 复杂场景 |

## 快速开始

### 1. 启动 Redis

使用 Docker 启动 Redis：

```bash
docker run -d -p 6379:6379 redis:latest
```

或使用 Docker Compose（在项目根目录）：

```bash
docker-compose up -d redis
```

### 2. 运行应用

```bash
cd redis-example
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 3. 测试接口

```bash
# Jedis 示例
curl "http://localhost:8080/redis/client/jedis/string"
curl "http://localhost:8080/redis/client/jedis/hash"
curl "http://localhost:8080/redis/client/jedis/lock"

# Lettuce 示例
curl "http://localhost:8080/redis/client/lettuce/string"
curl "http://localhost:8080/redis/client/lettuce/async"

# Redisson 示例
curl "http://localhost:8080/redis/client/redisson/bucket"
curl "http://localhost:8080/redis/client/redisson/lock"

# 工具类示例
curl "http://localhost:8080/redis/util/jedis/set?key=test&value=hello"
curl "http://localhost:8080/redis/util/compare/set"
```

## 工具类使用

项目提供了三个工具类，封装了常见的 Redis 操作：

- **JedisUtil** - Jedis 客户端工具类
- **LettuceUtil** - Lettuce 客户端工具类
- **RedissonUtil** - Redisson 客户端工具类

详细使用说明请查看：[UTIL_USAGE.md](UTIL_USAGE.md)

## API 端点

### Jedis 客户端

- `GET /redis/client/jedis/string` - String操作
- `GET /redis/client/jedis/hash` - Hash操作
- `GET /redis/client/jedis/list` - List操作
- `GET /redis/client/jedis/set` - Set操作
- `GET /redis/client/jedis/lock` - 分布式锁
- `GET /redis/client/jedis/pipeline` - 管道操作
- `GET /redis/client/jedis/transaction` - 事务操作

### Lettuce 客户端

- `GET /redis/client/lettuce/string` - 同步String操作
- `GET /redis/client/lettuce/hash` - 同步Hash操作
- `GET /redis/client/lettuce/list` - 同步List操作
- `GET /redis/client/lettuce/set` - 同步Set操作
- `GET /redis/client/lettuce/async` - 异步操作
- `GET /redis/client/lettuce/reactive` - 反应式操作
- `GET /redis/client/lettuce/lock` - 分布式锁
- `GET /redis/client/lettuce/batch` - 批量操作

### Redisson 客户端

- `GET /redis/client/redisson/bucket` - Bucket操作
- `GET /redis/client/redisson/map` - Map操作
- `GET /redis/client/redisson/list` - List操作
- `GET /redis/client/redisson/set` - Set操作
- `GET /redis/client/redisson/lock` - 分布式锁
- `GET /redis/client/redisson/fairlock` - 公平锁
- `GET /redis/client/redisson/readwritelock` - 读写锁
- `GET /redis/client/redisson/semaphore` - 信号量
- `GET /redis/client/redisson/bloomfilter` - 布隆过滤器
- `GET /redis/client/redisson/atomic` - 原子操作

### 工具类接口

- `GET /redis/util/jedis/set?key=xxx&value=xxx` - Jedis工具类设置
- `GET /redis/util/jedis/get?key=xxx` - Jedis工具类获取
- `GET /redis/util/lettuce/set?key=xxx&value=xxx` - Lettuce工具类设置
- `GET /redis/util/redisson/set?key=xxx&value=xxx` - Redisson工具类设置
- `GET /redis/util/compare/set` - 三种工具类对比
- `GET /redis/util/demo/overwrite` - Redis key覆盖行为演示

## 配置说明

配置文件：`src/main/resources/application.yml`

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 如果有密码，填写密码
    database: 0
```

## 项目结构

```
redis-example/
├── src/main/java/com/example/redis/
│   ├── RedisExampleApplication.java      # 启动类
│   ├── config/
│   │   └── RedisConfig.java             # Redis配置
│   ├── controller/
│   │   ├── RedisClientExampleController.java  # 客户端示例控制器
│   │   ├── RedisExampleController.java        # Redisson示例控制器
│   │   └── RedisUtilController.java           # 工具类控制器
│   ├── service/
│   │   ├── JedisExampleService.java     # Jedis示例服务
│   │   ├── LettuceExampleService.java   # Lettuce示例服务
│   │   └── RedissonExampleService.java  # Redisson示例服务
│   └── util/
│       ├── JedisUtil.java               # Jedis工具类
│       ├── LettuceUtil.java              # Lettuce工具类
│       └── RedissonUtil.java             # Redisson工具类
└── src/main/resources/
    └── application.yml                   # 配置文件
```

## 相关文档

- [工具类使用指南](UTIL_USAGE.md) - 工具类的详细使用方法
- [Redis Key覆盖行为说明](REDIS_KEY_OVERWRITE.md) - Redis重复设置key的行为说明
- [快速测试指南](QUICK_TEST.md) - 快速测试步骤
- [测试指南](TEST_GUIDE.md) - 详细测试说明

## 常见问题

### 端口冲突

如果 8080 端口被占用，可以修改 `application.yml` 中的端口配置，或查看 [PORT_CONFLICT.md](PORT_CONFLICT.md)

### Redis 连接失败

1. 确保 Redis 服务已启动
2. 检查 `application.yml` 中的 Redis 配置
3. 检查防火墙设置

## 学习建议

1. **初学者**：先看 `JedisExampleService`，了解基本的 Redis 操作
2. **进阶**：学习 `LettuceExampleService`，了解异步和反应式编程
3. **高级**：研究 `RedissonExampleService`，学习分布式锁等高级特性
4. **实践**：使用工具类封装自己的业务逻辑
