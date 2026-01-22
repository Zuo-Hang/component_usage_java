# MQ 使用示例模块

本模块演示了 RocketMQ 消息队列的使用方式，包括同步消息、异步消息、延迟消息、顺序消息等多种场景。

## 功能特性

- **同步消息**：保证消息发送成功的场景
- **异步消息**：高性能消息发送场景
- **单向消息**：日志等对可靠性要求不高的场景
- **延迟消息**：订单超时等延迟处理场景
- **顺序消息**：保证消息顺序处理的场景
- **批量消息**：批量发送消息
- **消息消费**：消息监听和处理

## 快速开始

### 1. 启动 RocketMQ

使用 Docker 启动 RocketMQ：

```bash
# 启动 NameServer
docker run -d -p 9876:9876 --name rmqnamesrv \
  apacherocketmq/rocketmq:latest sh mqnamesrv

# 启动 Broker
docker run -d -p 10909:10909 -p 10911:10911 --name rmqbroker \
  --link rmqnamesrv:namesrv \
  -e "NAMESRV_ADDR=namesrv:9876" \
  apacherocketmq/rocketmq:latest sh mqbroker -n namesrv:9876
```

或使用 Docker Compose（在项目根目录）：

```bash
docker-compose up -d rocketmq
```

### 2. 配置 RocketMQ

修改 `src/main/resources/application.yml`：

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: my-producer-group
```

### 3. 运行应用

```bash
cd mq-example
mvn spring-boot:run
```

应用将在 `http://localhost:8082` 启动。

### 4. 测试接口

```bash
# 发送同步消息
curl -X POST "http://localhost:8082/mq/sync?orderId=1001"

# 发送异步消息
curl -X POST "http://localhost:8082/mq/async?orderId=1001"

# 发送单向消息
curl -X POST "http://localhost:8082/mq/oneway?orderId=1001"

# 发送延迟消息
curl -X POST "http://localhost:8082/mq/delay?orderId=1001&delayLevel=3"

# 发送顺序消息
curl -X POST "http://localhost:8082/mq/orderly?orderId=1001"

# 批量发送消息
curl -X POST "http://localhost:8082/mq/batch"
```

## API 端点

- `POST /mq/sync?orderId=xxx` - 发送同步消息
- `POST /mq/async?orderId=xxx` - 发送异步消息
- `POST /mq/oneway?orderId=xxx` - 发送单向消息
- `POST /mq/delay?orderId=xxx&delayLevel=3` - 发送延迟消息
- `POST /mq/orderly?orderId=xxx` - 发送顺序消息
- `POST /mq/batch` - 批量发送消息

## 配置说明

配置文件：`src/main/resources/application.yml`

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: my-producer-group
  consumer:
    group: my-consumer-group
```

## 项目结构

```
mq-example/
├── src/main/java/com/example/mq/
│   ├── MQExampleApplication.java        # 启动类
│   ├── config/
│   │   └── RocketMQConfig.java          # RocketMQ配置
│   ├── controller/
│   │   └── MQExampleController.java     # MQ示例控制器
│   ├── model/
│   │   └── OrderMessage.java            # 订单消息模型
│   ├── service/
│   │   └── MQProducerService.java       # 消息生产者服务
│   └── consumer/
│       ├── OrderMessageConsumer.java    # 订单消息消费者
│       └── DelayMessageConsumer.java    # 延迟消息消费者
└── src/main/resources/
    └── application.yml                   # 配置文件
```

## 消息类型说明

### 同步消息
- **特点**：发送后等待响应，保证消息发送成功
- **适用场景**：对可靠性要求高的场景，如订单创建
- **性能**：较低，需要等待响应

### 异步消息
- **特点**：发送后立即返回，通过回调处理结果
- **适用场景**：高性能要求的场景，如日志记录
- **性能**：高，不阻塞主流程

### 单向消息
- **特点**：发送后不等待响应，不关心发送结果
- **适用场景**：对可靠性要求不高的场景，如日志
- **性能**：最高，完全不阻塞

### 延迟消息
- **特点**：消息延迟指定时间后投递
- **适用场景**：订单超时、定时任务等
- **延迟级别**：1-18，对应不同的延迟时间

### 顺序消息
- **特点**：保证消息按顺序消费
- **适用场景**：需要保证顺序的场景，如订单状态变更
- **注意**：需要指定消息队列选择器

### 批量消息
- **特点**：一次发送多条消息
- **适用场景**：批量操作，提高发送效率
- **限制**：单次批量消息总大小不超过 4MB

## 消息消费

消息消费者通过 `@RocketMQMessageListener` 注解监听消息：

```java
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "my-consumer-group"
)
public class OrderMessageConsumer implements RocketMQListener<OrderMessage> {
    @Override
    public void onMessage(OrderMessage message) {
        // 处理消息
    }
}
```

## 常见问题

### RocketMQ 连接失败

1. 确保 RocketMQ NameServer 已启动
2. 检查 `application.yml` 中的 NameServer 地址
3. 确认端口 9876 可访问

### 消息发送失败

1. 检查 Topic 是否存在
2. 确认 Producer Group 配置正确
3. 查看 RocketMQ 日志

### 消息消费失败

1. 检查 Consumer Group 配置
2. 确认 Topic 和 Tag 匹配
3. 查看消费者日志

## 学习建议

1. **初学者**：先了解同步消息和异步消息的区别
2. **进阶**：学习延迟消息和顺序消息的使用场景
3. **高级**：研究消息事务、消息过滤等高级特性
4. **实践**：根据实际业务场景选择合适的消息类型
