# Java组件使用示例项目

这是一个标准的Maven多模块项目，展示了Java中常用组件的使用方法和最佳实践。

## 项目结构

```
component-usage-java/
├── pom.xml                          # 父POM文件
├── redis-example/                   # Redis使用示例模块
├── mysql-example/                   # MySQL使用示例模块
├── mq-rocketmq-example/             # RocketMQ消息队列使用示例模块
├── mq-kafka-example/                # Kafka消息队列使用示例模块
└── zookeeper-example/               # ZooKeeper使用示例模块
```

## 模块说明

### 1. redis-example
Redis使用示例模块，演示了三种客户端（Jedis、Lettuce、Redisson）的使用方式。

**详细文档**: [redis-example/README.md](redis-example/README.md)

### 2. mysql-example
MySQL使用示例模块，演示了三种数据库框架（JDBC Template、MyBatis、MyBatis Plus）的使用方式。

**详细文档**: [mysql-example/README.md](mysql-example/README.md)

### 3. mq-rocketmq-example
RocketMQ消息队列使用示例模块，演示了各种消息发送和消费场景。

**详细文档**: [mq-rocketmq-example/README.md](mq-rocketmq-example/README.md)

### 4. mq-kafka-example
Kafka消息队列使用示例模块，演示了同步/异步发送、消费者监听、分区控制等场景。

**详细文档**: [mq-kafka-example/README.md](mq-kafka-example/README.md)

### 5. zookeeper-example
ZooKeeper使用示例模块，演示了节点操作、监听机制等场景。

**详细文档**: [zookeeper-example/README.md](zookeeper-example/README.md)

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.2.0
- **Maven**: 多模块项目管理
- **Redis**: Jedis 4.3.1, Lettuce, Redisson
- **MySQL**: MySQL Connector 8.0.33
- **MyBatis**: 2.3.1
- **MyBatis Plus**: 3.5.3
- **RocketMQ**: 2.2.3
- **Kafka**: Spring Kafka (Spring Boot 管理版本)
- **ZooKeeper**: Apache Curator 5.5.0
- **HikariCP**: 数据库连接池
- **Lombok**: 简化Java代码

## 快速开始

### 前置要求

1. **JDK 21+** (Java 21 或更高版本)
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
```

详细说明请查看 [docker-compose.README.md](docker-compose.README.md)

### 构建项目

```bash
# 在项目根目录执行
mvn clean install
```

### 运行模块

每个模块都可以独立运行，详细说明请查看各模块的 README.md：

```bash
# 运行Redis示例
cd redis-example
mvn spring-boot:run

# 运行MySQL示例
cd mysql-example
mvn spring-boot:run

# 运行RocketMQ示例
cd mq-rocketmq-example
mvn spring-boot:run

# 运行Kafka示例
cd mq-kafka-example
mvn spring-boot:run

# 运行ZooKeeper示例
cd zookeeper-example
mvn spring-boot:run
```

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
5. 创建模块的 README.md 文档

## 许可证

本项目仅供学习和参考使用。
