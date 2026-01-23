# Kafka 消费者可靠性保障指南

本文档详细说明如何保障 Kafka 消费者消费消息的可靠性，重点关注：**消费不丢、不重、顺序消费**。

## 一、消费不丢（消息不丢失）

### 1. 使用手动确认（Manual Acknowledgment）

**核心原则**：只有消息处理成功后才提交 offset。

#### 配置方式

```java
// 消费者配置
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

// 监听器配置
factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
```

#### 代码示例

```java
@KafkaListener(topics = "order-topic", 
               containerFactory = "manualAckKafkaListenerContainerFactory")
public void consumeOrderTopic(
        @Payload String message,
        Acknowledgment acknowledgment) {
    try {
        // 业务处理
        processMessage(message);
        
        // 只有处理成功才确认
        acknowledgment.acknowledge();
    } catch (Exception e) {
        // 处理失败时不确认，消息会重新消费
        log.error("处理失败", e);
        throw e; // 重新抛出异常
    }
}
```

### 2. 确认模式对比

| 确认模式 | 说明 | 可靠性 | 性能 |
|---------|------|--------|------|
| `AUTO_COMMIT` | 自动提交，可能丢失消息 | ⭐ | ⭐⭐⭐⭐⭐ |
| `MANUAL` | 批量确认，处理完一批后确认 | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| `MANUAL_IMMEDIATE` | 立即确认，处理完一条确认一条 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

**推荐**：关键业务使用 `MANUAL_IMMEDIATE`

### 3. 关键配置

```java
// 1. 禁用自动提交
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

// 2. 设置会话超时（防止误判为离线）
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30秒

// 3. 设置心跳间隔
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3秒

// 4. 设置最大拉取间隔（处理时间不能超过此值）
props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5分钟
```

### 4. 注意事项

- **处理时间限制**：消息处理时间不能超过 `MAX_POLL_INTERVAL_MS`，否则会被认为离线
- **异常处理**：处理失败时不要确认，让消息重新消费
- **幂等性**：由于消息可能重新消费，业务逻辑必须支持幂等性

## 二、消费不重（防止重复消费）

### 1. 幂等性检查

**核心原则**：使用业务唯一标识判断消息是否已处理。

#### 实现方式

```java
// 方式1：基于消息ID（推荐）
String messageId = String.format("%s-%d-%d", topic, partition, offset);

// 方式2：基于业务唯一标识（从消息中提取）
String businessId = extractBusinessId(message);

// 检查是否已处理
if (processedMessageIds.contains(messageId)) {
    log.warn("消息已处理过，跳过: messageId={}", messageId);
    acknowledgment.acknowledge(); // 确认消息，避免重复处理
    return;
}

// 处理消息
processMessage(message);

// 记录已处理
processedMessageIds.add(messageId);
```

### 2. 幂等性存储方案

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| **内存 Set** | 简单快速 | 重启丢失，单机 | 开发测试 |
| **Redis** | 性能好，支持分布式 | 需要额外服务 | 生产环境推荐 ✅ |
| **数据库** | 持久化，可靠 | 性能较低 | 关键业务 |
| **本地文件** | 简单 | 单机，性能差 | 不推荐 |

#### Redis 实现示例

```java
@Autowired
private RedisTemplate<String, String> redisTemplate;

private boolean isProcessed(String messageId) {
    String key = "kafka:processed:" + messageId;
    Boolean exists = redisTemplate.hasKey(key);
    if (Boolean.TRUE.equals(exists)) {
        return true;
    }
    // 设置过期时间（如7天）
    redisTemplate.opsForValue().set(key, "1", Duration.ofDays(7));
    return false;
}
```

### 3. 业务层幂等性

除了消息级别的幂等性，业务逻辑本身也应该支持幂等性：

```java
// 示例：订单处理
public void processOrder(Order order) {
    // 1. 检查订单是否已处理（数据库唯一索引）
    if (orderRepository.existsById(order.getId())) {
        log.warn("订单已处理: orderId={}", order.getId());
        return;
    }
    
    // 2. 处理订单（数据库唯一索引保证不重复）
    orderRepository.save(order);
}
```

## 三、顺序消费

### 1. 顺序消费的三种场景

#### 场景1：全局顺序（所有消息严格顺序）

**实现方式**：
- Topic 只有 1 个分区
- 消费者并发数为 1
- 每次只拉取 1 条消息

```java
// 配置
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

// 监听器
factory.setConcurrency(1); // 单线程
```

**适用场景**：很少使用，性能低

#### 场景2：分区内顺序（推荐）

**实现方式**：
- 使用相同的 key 发送消息（相同 key 会到同一分区）
- 每个分区一个消费者线程
- 分区内消息自然有序

```java
// 生产者：使用相同的 key
kafkaTemplate.send(topic, orderId, message);

// 消费者：每个分区一个线程
factory.setConcurrency(3); // 3个分区，3个线程
```

**适用场景**：按业务实体（如订单ID）顺序处理 ✅

#### 场景3：按 key 顺序

**实现方式**：
- 相同 key 的消息发送到同一分区
- 不同 key 可以并行处理
- 相同 key 的消息按顺序处理

```java
// 生产者：使用业务 key
kafkaTemplate.send(topic, userId, message);

// 消费者：自动按分区顺序处理
@KafkaListener(topics = "user-topic", 
               containerFactory = "manualAckKafkaListenerContainerFactory")
public void consumeByKey(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        Acknowledgment acknowledgment) {
    // 相同 key 的消息会按顺序处理
    processMessage(key, message);
    acknowledgment.acknowledge();
}
```

**适用场景**：按用户、订单等业务实体顺序处理 ✅

### 2. 顺序消费配置

```java
// 1. 每次只拉取1条消息（严格顺序）
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

// 2. 单线程处理（全局顺序）
factory.setConcurrency(1);

// 3. 手动确认，立即确认
factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
```

### 3. 顺序消费注意事项

- **性能权衡**：顺序消费会降低吞吐量，需要权衡
- **失败处理**：如果某条消息处理失败，后续消息会阻塞
- **分区策略**：确保相同 key 的消息发送到同一分区

## 四、完整可靠性保障方案

### 1. 配置清单

```java
// 消费者配置（高可靠性）
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 手动提交
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // 从最早开始
props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30秒
props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3秒
props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5分钟
props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // 每次10条

// 监听器配置
factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
factory.setConcurrency(3); // 根据分区数设置
```

### 2. 代码模板

```java
@KafkaListener(topics = "order-topic", 
               containerFactory = "manualAckKafkaListenerContainerFactory")
public void consumeOrderTopic(
        @Payload String message,
        Acknowledgment acknowledgment,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset) {
    
    // 1. 生成消息ID
    String messageId = String.format("%s-%d-%d", "order-topic", partition, offset);
    
    try {
        // 2. 幂等性检查
        if (isProcessed(messageId)) {
            log.warn("消息已处理，跳过: messageId={}", messageId);
            acknowledgment.acknowledge();
            return;
        }
        
        // 3. 业务处理
        processMessage(message);
        
        // 4. 记录已处理
        markAsProcessed(messageId);
        
        // 5. 确认消息
        acknowledgment.acknowledge();
        
    } catch (Exception e) {
        log.error("处理失败: messageId={}", messageId, e);
        // 不确认，重新消费
        throw new RuntimeException("处理失败", e);
    }
}
```

## 五、错误处理和重试

### 1. 重试策略

```java
@KafkaListener(topics = "order-topic")
public void consumeWithRetry(
        @Payload String message,
        Acknowledgment acknowledgment) {
    
    int maxRetries = 3;
    int attempts = 0;
    
    while (attempts < maxRetries) {
        try {
            processMessage(message);
            acknowledgment.acknowledge();
            return;
        } catch (Exception e) {
            attempts++;
            if (attempts >= maxRetries) {
                // 重试失败，发送到死信队列
                sendToDeadLetterQueue(message, e);
                acknowledgment.acknowledge(); // 确认消息，避免无限重试
            } else {
                // 等待后重试
                Thread.sleep(1000 * attempts);
            }
        }
    }
}
```

### 2. 死信队列

对于重试多次仍失败的消息，应发送到死信队列：

```java
private void sendToDeadLetterQueue(String message, Exception error) {
    // 1. 记录到数据库
    deadLetterRepository.save(new DeadLetter(message, error));
    
    // 2. 发送到死信Topic
    kafkaTemplate.send("dead-letter-topic", message);
    
    // 3. 发送告警
    alertService.sendAlert("消息处理失败", message, error);
}
```

## 六、最佳实践总结

### ✅ 消费不丢

1. **使用手动确认**：`ENABLE_AUTO_COMMIT=false`
2. **处理成功才确认**：`acknowledgment.acknowledge()`
3. **异常不确认**：处理失败时不确认，让消息重新消费
4. **合理设置超时**：`MAX_POLL_INTERVAL_MS` 要大于处理时间

### ✅ 消费不重

1. **幂等性检查**：使用消息ID或业务ID判断是否已处理
2. **持久化存储**：使用 Redis 或数据库存储已处理消息
3. **业务层幂等**：业务逻辑本身支持幂等性（如数据库唯一索引）

### ✅ 顺序消费

1. **按 key 分区**：相同 key 的消息发送到同一分区
2. **分区内顺序**：每个分区一个消费者线程
3. **单条处理**：`MAX_POLL_RECORDS=1`（严格顺序）
4. **立即确认**：`MANUAL_IMMEDIATE` 模式

### ⚠️ 注意事项

1. **性能权衡**：可靠性越高，性能越低
2. **处理时间**：不能超过 `MAX_POLL_INTERVAL_MS`
3. **并发控制**：顺序消费时并发数 = 分区数
4. **监控告警**：监控消费延迟、失败率等指标

## 七、配置对比表

| 场景 | 确认模式 | 并发数 | MAX_POLL_RECORDS | 幂等性 | 顺序性 |
|------|---------|--------|----------------|--------|--------|
| **非关键业务** | AUTO_COMMIT | 3+ | 100+ | 不需要 | 不需要 |
| **关键业务** | MANUAL_IMMEDIATE | 3 | 10 | 需要 ✅ | 不需要 |
| **顺序业务** | MANUAL_IMMEDIATE | 1 | 1 | 需要 ✅ | 需要 ✅ |
| **按key顺序** | MANUAL_IMMEDIATE | 分区数 | 10 | 需要 ✅ | 按key ✅ |

## 八、参考资源

- [Kafka Consumer Configs](https://kafka.apache.org/documentation/#consumerconfigs)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Exactly-Once Semantics](https://kafka.apache.org/documentation/#semantics)
