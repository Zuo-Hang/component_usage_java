# Kafka 使用示例

本模块演示了 Apache Kafka 消息队列的使用方式，包括同步/异步发送、消费者监听、分区控制等多种场景。

## 功能特性

- ✅ **同步发送消息** - 保证消息发送成功的场景
- ✅ **异步发送消息** - 高性能消息发送场景
- ✅ **指定分区发送** - 控制消息路由到特定分区
- ✅ **消息消费** - 自动确认和手动确认两种模式
- ✅ **多主题监听** - 监听多个主题的消息
- ✅ **Spring Kafka 集成** - 使用 Spring Boot Starter

## 技术栈

- **Spring Boot 3.2.0**
- **Spring Kafka** - Kafka 集成框架
- **Apache Kafka** - 分布式流处理平台

## 快速开始

### 1. 启动 Kafka

使用 Docker 启动 Kafka：

```bash
# 使用项目中的 docker-compose
docker-compose up -d kafka

# 或者手动启动
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk \
  apache/kafka:latest
```

### 2. 创建主题（可选）

Kafka 会自动创建主题，但也可以手动创建：

```bash
# 进入 Kafka 容器
docker exec -it kafka bash

# 创建主题
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --partitions 3 \
  --replication-factor 1

# 查看主题列表
kafka-topics.sh --list --bootstrap-server localhost:9092
```

### 3. 启动应用

```bash
cd mq-kafka-example
mvn spring-boot:run
```

应用将在 `http://localhost:8084` 启动。

## API 示例

### 同步发送消息

```bash
curl -X POST "http://localhost:8084/kafka/send/sync?topic=test-topic&key=key1&message=hello%20world"
```

### 异步发送消息

```bash
curl -X POST "http://localhost:8084/kafka/send/async?topic=test-topic&key=key1&message=hello%20world"
```

### 发送消息（无键）

```bash
curl -X POST "http://localhost:8084/kafka/send?topic=test-topic&message=hello%20world"
```

### 发送消息到指定分区

```bash
curl -X POST "http://localhost:8084/kafka/send/partition?topic=test-topic&partition=0&key=key1&message=hello%20world"
```

## 配置说明

在 `application.yml` 中可以配置 Kafka 连接参数：

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all              # 等待所有副本确认
      retries: 3              # 重试次数
      enable-idempotence: true # 幂等性
    consumer:
      group-id: default-group # 消费者组ID
      auto-offset-reset: earliest # 从最早的消息开始消费
      enable-auto-commit: true   # 自动提交偏移量
```

## 核心概念

### 1. 主题（Topic）
- 消息的分类，类似于数据库的表
- 可以设置分区数和副本数

### 2. 分区（Partition）
- 主题的物理分割，提高并发性能
- 消息按 key 的 hash 值路由到不同分区

### 3. 消费者组（Consumer Group）
- 多个消费者可以组成一个消费者组
- 组内消费者共同消费主题的消息，实现负载均衡

### 4. 偏移量（Offset）
- 消费者在分区中的读取位置
- 可以自动提交或手动提交

## 消息发送模式

### 同步发送
- 等待消息发送完成并返回结果
- 保证消息发送成功
- 性能相对较低

### 异步发送
- 立即返回，不等待发送结果
- 通过回调处理发送结果
- 性能较高

## 消息消费模式

### 自动确认
- 消费者自动提交偏移量
- 配置简单，适合大多数场景
- 可能丢失消息（如果处理失败）

### 手动确认
- 消费者手动提交偏移量
- 保证消息处理完成后再提交
- 适合对可靠性要求高的场景

## Docker Compose 配置

在 `docker-compose.yml` 中已添加 Kafka 服务：

```yaml
kafka:
  image: apache/kafka:latest
  container_name: kafka
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    # ... 其他配置
  volumes:
    - kafka-data:/var/lib/kafka/data
  networks:
    - component-network
```

## 注意事项

1. **主题创建** - Kafka 会自动创建主题，但建议手动创建以设置合适的分区数
2. **消费者组** - 不同消费者组可以独立消费同一主题
3. **偏移量管理** - 注意偏移量的提交时机，避免重复消费或丢失消息
4. **分区策略** - 相同 key 的消息会路由到同一分区，保证顺序
5. **性能优化** - 批量发送、压缩、异步发送等可以提高性能

## 与 RocketMQ 的对比

| 特性 | Kafka | RocketMQ |
|------|-------|----------|
| **定位** | 分布式流处理平台 | 消息中间件 |
| **吞吐量** | 极高 | 高 |
| **延迟** | 毫秒级 | 毫秒级 |
| **顺序消息** | 支持（分区内） | 支持 |
| **事务消息** | 支持 | 支持 |
| **适用场景** | 大数据流处理、日志收集 | 业务消息、订单处理 |

## 参考文档

- [Apache Kafka 官方文档](https://kafka.apache.org/documentation/)
- [Spring Kafka 官方文档](https://docs.spring.io/spring-kafka/reference/html/)
- [Kafka 快速入门](https://kafka.apache.org/quickstart)
