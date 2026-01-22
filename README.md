# Java组件使用示例项目

这是一个标准的Maven多模块项目，展示了Java中常用组件的使用方法和最佳实践。

## 项目结构

```
component-usage-java/
├── pom.xml                          # 父POM文件
├── redis-example/                   # Redis使用示例模块
│   ├── pom.xml
│   └── src/main/java/com/example/redis/
│       ├── RedisExampleApplication.java
│       ├── config/RedisConfig.java
│       ├── service/RedisExampleService.java
│       └── controller/RedisExampleController.java
├── mysql-example/                   # MySQL使用示例模块
│   ├── pom.xml
│   └── src/main/java/com/example/mysql/
│       ├── MySQLExampleApplication.java
│       ├── model/User.java
│       ├── mapper/UserMapper.java
│       ├── service/MySQLExampleService.java
│       └── controller/MySQLExampleController.java
└── mq-example/                      # 消息队列使用示例模块
    ├── pom.xml
    └── src/main/java/com/example/mq/
        ├── MQExampleApplication.java
        ├── config/RocketMQConfig.java
        ├── model/OrderMessage.java
        ├── service/MQProducerService.java
        ├── consumer/OrderMessageConsumer.java
        └── controller/MQExampleController.java
```

## 模块说明

### 1. redis-example
Redis使用示例模块，演示了**三种客户端**（Jedis、Lettuce、Redisson）的使用方式：

#### Jedis客户端示例
- **String操作**：基本的键值对操作、设置过期时间、递增操作
- **Hash操作**：存储对象数据
- **List操作**：队列和栈的操作
- **Set操作**：集合操作
- **分布式锁**：使用SET NX EX命令实现
- **管道操作**：批量操作示例
- **事务操作**：Redis事务示例

**API端点**:
- `GET /redis/client/jedis/string` - String操作
- `GET /redis/client/jedis/hash` - Hash操作
- `GET /redis/client/jedis/list` - List操作
- `GET /redis/client/jedis/set` - Set操作
- `GET /redis/client/jedis/lock` - 分布式锁
- `GET /redis/client/jedis/pipeline` - 管道操作
- `GET /redis/client/jedis/transaction` - 事务操作

#### Lettuce客户端示例
- **同步操作**：String、Hash、List、Set等基本操作
- **异步操作**：基于Future的异步非阻塞操作
- **反应式操作**：基于Reactor的反应式编程
- **分布式锁**：使用SET NX EX命令实现
- **批量操作**：管道批量执行

**API端点**:
- `GET /redis/client/lettuce/string` - 同步String操作
- `GET /redis/client/lettuce/hash` - 同步Hash操作
- `GET /redis/client/lettuce/list` - 同步List操作
- `GET /redis/client/lettuce/set` - 同步Set操作
- `GET /redis/client/lettuce/async` - 异步操作
- `GET /redis/client/lettuce/reactive` - 反应式操作
- `GET /redis/client/lettuce/lock` - 分布式锁
- `GET /redis/client/lettuce/batch` - 批量操作

#### Redisson客户端示例（推荐）
- **分布式对象**：RBucket、RMap、RList、RSet等
- **分布式锁**：RLock（支持可重入、自动续期）、公平锁、读写锁
- **信号量**：RSemaphore（限流）
- **布隆过滤器**：RBloomFilter
- **原子操作**：RAtomicLong等

**API端点**:
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

**对比接口**:
- `GET /redis/client/compare/string` - 三种客户端String操作对比
- `GET /redis/client/compare/lock` - 三种客户端分布式锁对比

**运行端口**: 8080

**配置**: `redis-example/src/main/resources/application.yml`

**说明**: 三种客户端可以在同一个项目中同时使用，不会相互影响。每种客户端都有独立的连接池和配置。

### 2. mysql-example
MySQL使用示例模块，演示了**三种数据库框架**（JDBC Template、MyBatis、MyBatis Plus）的使用方式：

#### JDBC Template示例
- **直接SQL操作**：完全控制SQL语句
- **批量操作**：批量插入、更新
- **复杂查询**：动态SQL拼接

**API端点**:
- `POST /mysql/framework/jdbc/user` - 创建用户
- `GET /mysql/framework/jdbc/user/{id}` - 根据ID查询
- `GET /mysql/framework/jdbc/users` - 查询所有用户
- `PUT /mysql/framework/jdbc/user/{id}` - 更新用户
- `DELETE /mysql/framework/jdbc/user/{id}` - 删除用户
- `GET /mysql/framework/jdbc/users/age/{age}` - 根据年龄查询
- `GET /mysql/framework/jdbc/count` - 获取用户总数
- `POST /mysql/framework/jdbc/batch` - 批量插入
- `GET /mysql/framework/jdbc/users/conditions` - 复杂条件查询

#### MyBatis示例
- **注解方式**：使用@Select、@Insert、@Update、@Delete注解
- **SQL映射**：将SQL与Java方法映射
- **参数绑定**：使用#{参数}绑定

**API端点**:
- `POST /mysql/framework/mybatis/user` - 创建用户
- `GET /mysql/framework/mybatis/user/{id}` - 根据ID查询
- `GET /mysql/framework/mybatis/users` - 查询所有用户
- `PUT /mysql/framework/mybatis/user/{id}` - 更新用户
- `DELETE /mysql/framework/mybatis/user/{id}` - 删除用户
- `GET /mysql/framework/mybatis/user/username/{username}` - 根据用户名查询

#### MyBatis Plus示例（推荐）
- **自动CRUD**：无需编写SQL，自动生成CRUD方法
- **条件构造器**：LambdaQueryWrapper类型安全的条件查询
- **分页插件**：内置分页功能
- **字段填充**：自动填充createTime、updateTime
- **批量操作**：内置批量插入、更新、删除

**API端点**:
- `POST /mysql/framework/mybatis-plus/user` - 创建用户
- `GET /mysql/framework/mybatis-plus/user/{id}` - 根据ID查询
- `GET /mysql/framework/mybatis-plus/users` - 查询所有用户
- `PUT /mysql/framework/mybatis-plus/user/{id}` - 更新用户
- `DELETE /mysql/framework/mybatis-plus/user/{id}` - 删除用户
- `GET /mysql/framework/mybatis-plus/users/age/{age}` - 根据年龄查询
- `GET /mysql/framework/mybatis-plus/users/conditions` - 复杂条件查询
- `GET /mysql/framework/mybatis-plus/users/page` - 分页查询
- `GET /mysql/framework/mybatis-plus/users/page/condition` - 分页条件查询
- `POST /mysql/framework/mybatis-plus/batch` - 批量插入
- `PUT /mysql/framework/mybatis-plus/batch/age` - 批量更新年龄
- `GET /mysql/framework/mybatis-plus/count/age/{age}` - 统计查询
- `GET /mysql/framework/mybatis-plus/exists/username/{username}` - 存在性查询

#### 框架对比接口
- `GET /mysql/framework/compare/create` - 创建操作对比
- `GET /mysql/framework/compare/query` - 查询操作对比
- `GET /mysql/framework/compare/condition` - 条件查询对比
- `GET /mysql/framework/compare/features` - 功能特性对比

**运行端口**: 8081

**配置**: `mysql-example/src/main/resources/application.yml`

**数据库初始化**: 执行 `mysql-example/src/main/resources/schema.sql` 创建用户表

**框架选择建议**:
- **JDBC Template**：适合需要完全控制SQL的场景，简单的CRUD操作
- **MyBatis**：适合复杂SQL查询，需要SQL与代码分离的场景
- **MyBatis Plus**：适合90%的CRUD场景，快速开发，无需手写SQL

### 3. mq-example
消息队列使用示例模块（基于RocketMQ），演示了以下功能：
- **同步消息**：保证消息发送成功的场景
- **异步消息**：高性能消息发送场景
- **单向消息**：日志等对可靠性要求不高的场景
- **延迟消息**：订单超时等延迟处理场景
- **顺序消息**：保证消息顺序处理的场景
- **批量消息**：批量发送消息
- **消息消费**：消息监听和处理

**运行端口**: 8082

**配置**: `mq-example/src/main/resources/application.yml`

**需要启动RocketMQ**: 确保RocketMQ NameServer运行在 `localhost:9876`

**API示例**:
- `POST /mq/sync?orderId=1001` - 发送同步消息
- `POST /mq/async?orderId=1001` - 发送异步消息
- `POST /mq/oneway?orderId=1001` - 发送单向消息
- `POST /mq/delay?orderId=1001&delayLevel=3` - 发送延迟消息
- `POST /mq/orderly?orderId=1001` - 发送顺序消息
- `POST /mq/batch` - 批量发送消息

## 技术栈

- **Java**: 1.8
- **Spring Boot**: 2.7.14
- **Maven**: 多模块项目管理
- **Redis**: Jedis 4.3.1
- **MySQL**: MySQL Connector 8.0.33
- **MyBatis**: 2.3.1
- **RocketMQ**: 2.2.3
- **HikariCP**: 数据库连接池
- **Lombok**: 简化Java代码

## 快速开始

### 前置要求

1. **JDK 1.8+**
2. **Maven 3.6+**
3. **Docker 和 Docker Compose**（推荐，用于快速启动依赖服务）
   - 或者手动安装：**Redis**、**MySQL**、**RocketMQ**

### 使用 Docker Compose 启动依赖服务（推荐）

项目提供了 `docker-compose.yml` 文件，可以一键启动所有依赖服务：

```bash
# 启动所有服务（Redis、MySQL、RocketMQ）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 只启动 Redis 和 MySQL（这两个通常没问题）
docker-compose up -d redis mysql

```

详细说明请查看 [docker-compose.README.md](docker-compose.README.md)

### 手动安装依赖服务

如果不使用 Docker Compose，需要手动安装：
1. **Redis** (redis-example模块需要)
2. **MySQL** (mysql-example模块需要)
3. **RocketMQ** (mq-example模块需要)

### 构建项目

```bash
# 在项目根目录执行
mvn clean install
```

### 运行模块

每个模块都可以独立运行：

```bash
# 运行Redis示例
cd redis-example
mvn spring-boot:run

# 运行MySQL示例
cd mysql-example
mvn spring-boot:run

# 运行MQ示例
cd mq-example
mvn spring-boot:run
```

### 配置说明

每个模块的配置文件位于 `src/main/resources/application.yml`，请根据实际环境修改：

1. **Redis配置** (redis-example):
   - 修改 `spring.redis.host` 和 `spring.redis.port`

2. **MySQL配置** (mysql-example):
   - 修改 `spring.datasource.url`
   - 修改 `spring.datasource.username` 和 `spring.datasource.password`
   - 执行 `schema.sql` 创建表结构

3. **RocketMQ配置** (mq-example):
   - 修改 `rocketmq.name-server` 为实际的NameServer地址

## 项目特点

- ✅ 标准Maven多模块项目结构
- ✅ 每个模块独立运行，互不依赖
- ✅ 完整的代码示例和注释
- ✅ 配置文件示例
- ✅ RESTful API示例
- ✅ 最佳实践展示

## 扩展模块

如果需要添加新的组件示例模块，按照以下步骤：

1. 在根 `pom.xml` 的 `<modules>` 中添加新模块
2. 创建新模块目录和 `pom.xml`
3. 按照标准Maven项目结构创建代码
4. 参考现有模块的代码风格和结构

## 许可证

本项目仅供学习和参考使用。

