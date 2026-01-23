# Kafka 生产者可靠性保障指南

本文档详细说明如何保障 Kafka 生产者发送消息的可靠性。

## 一、可靠性保障机制

### 1. 确认机制 (Acks)

**配置项**: `acks`

- **`acks=0`**: 不等待确认，性能最高但可能丢失消息
- **`acks=1`**: 等待 Leader 确认，平衡性能和可靠性
- **`acks=all` / `acks=-1`**: 等待所有 ISR（In-Sync Replicas）副本确认，**最高可靠性** ✅

**当前配置**: `acks=all`

### 2. 幂等性 (Idempotence)

**配置项**: `enable.idempotence=true`

**作用**:
- 防止消息重复发送（即使网络重传也不会产生重复消息）
- 自动设置 `max.in.flight.requests.per.connection=5`，保证分区内消息顺序
- 自动设置 `retries=Integer.MAX_VALUE`，无限重试直到成功

**当前配置**: `enable.idempotence=true` ✅

### 3. 重试机制

**配置项**:
- `retries`: 重试次数（启用幂等性后自动为 `Integer.MAX_VALUE`）
- `retry.backoff.ms`: 重试间隔（默认 100ms）

**当前配置**:
- `retries=2147483647` (Integer.MAX_VALUE) ✅
- `retry.backoff.ms=100` ✅

### 4. 超时配置

**配置项**:
- `request.timeout.ms`: 单次请求超时时间（默认 30 秒）
- `delivery.timeout.ms`: 总交付超时时间（默认 2 分钟）

**当前配置**:
- `request.timeout.ms=30000` ✅
- `delivery.timeout.ms=120000` ✅

### 5. 批量发送优化

**配置项**:
- `batch.size`: 批量大小（16KB）
- `linger.ms`: 等待时间以批量发送（10ms）
- `buffer.memory`: 缓冲区大小（32MB）

**作用**: 提高吞吐量，同时保证可靠性

**当前配置**:
- `batch.size=16384` ✅
- `linger.ms=10` ✅
- `buffer.memory=33554432` ✅

### 6. 压缩配置

**配置项**: `compression.type`

**可选值**: `none`, `gzip`, `snappy`, `lz4`, `zstd`

**作用**: 减少网络传输数据量，提高传输效率

**当前配置**: `compression.type=snappy` ✅

## 二、代码层面的可靠性保障

### 1. 同步发送 (`sendSync`)

```java
SendResult<String, String> result = kafkaProducerService.sendSync(topic, key, message);
```

**特点**:
- 等待消息确认后才返回
- 设置 30 秒超时，避免无限等待
- 详细的异常处理和日志记录
- **适合**: 对可靠性要求极高的场景（如支付、订单等）

### 2. 异步发送 (`sendAsync`)

```java
CompletableFuture<SendResult<String, String>> future = 
    kafkaProducerService.sendAsync(topic, key, message);
future.thenAccept(result -> {
    // 处理成功
}).exceptionally(ex -> {
    // 处理失败
    return null;
});
```

**特点**:
- 非阻塞，高吞吐量
- 通过 `CompletableFuture` 提供回调处理
- 详细的成功/失败日志
- **适合**: 高吞吐量场景，对延迟敏感

### 3. 业务层重试 (`sendSyncWithRetry`)

```java
SendResult<String, String> result = kafkaProducerService.sendSyncWithRetry(
    topic, key, message, maxRetries);
```

**特点**:
- 在 Kafka 自动重试基础上，增加业务层重试
- 指数退避策略（100ms, 200ms, 300ms...）
- **适合**: 网络抖动等临时性故障

## 三、可靠性级别对比

| 配置组合 | 可靠性 | 性能 | 适用场景 |
|---------|--------|------|---------|
| `acks=0` | ⭐ | ⭐⭐⭐⭐⭐ | 日志收集（允许丢失） |
| `acks=1` | ⭐⭐ | ⭐⭐⭐⭐ | 一般业务（可容忍少量丢失） |
| `acks=all` + `enable.idempotence=false` | ⭐⭐⭐ | ⭐⭐⭐ | 重要业务 |
| **`acks=all` + `enable.idempotence=true`** | **⭐⭐⭐⭐⭐** | **⭐⭐⭐** | **关键业务（当前配置）** ✅ |

## 四、最佳实践

### 1. 根据业务场景选择发送方式

- **关键业务**（支付、订单）: 使用 `sendSync()` 同步发送
- **高吞吐业务**（日志、埋点）: 使用 `sendAsync()` 异步发送
- **网络不稳定环境**: 使用 `sendSyncWithRetry()` 带重试的同步发送

### 2. 监控和告警

建议监控以下指标：
- 消息发送成功率
- 消息发送延迟（P50, P95, P99）
- 重试次数和失败率
- Kafka Producer 指标（通过 JMX）

### 3. 失败处理策略

对于最终发送失败的消息，可以考虑：
1. **持久化到数据库**: 记录失败消息，后续补偿处理
2. **死信队列**: 发送到专门的死信 Topic，单独处理
3. **告警通知**: 发送告警，人工介入处理

### 4. Topic 配置建议

为了配合生产者的可靠性配置，建议 Topic 配置：
- `replication.factor >= 3`: 至少 3 个副本
- `min.insync.replicas >= 2`: 至少 2 个 ISR 副本
- `unclean.leader.election.enable=false`: 禁止非 ISR 副本成为 Leader

## 五、常见问题

### Q1: 为什么启用幂等性后 retries 自动设置为 Integer.MAX_VALUE？

**A**: 幂等性保证即使重试也不会产生重复消息，因此可以安全地无限重试直到成功。

### Q2: acks=all 会影响性能吗？

**A**: 会，但影响可控。`acks=all` 需要等待所有 ISR 副本确认，会增加延迟（通常几毫秒到几十毫秒），但能保证最高可靠性。

### Q3: 如何平衡可靠性和性能？

**A**: 
- 关键业务：优先可靠性，使用 `acks=all` + 同步发送
- 一般业务：平衡两者，使用 `acks=1` + 异步发送
- 日志/埋点：优先性能，使用 `acks=0` + 异步发送

### Q4: 消息发送失败后如何处理？

**A**: 
1. Kafka 会自动重试（已配置）
2. 业务层可以调用 `sendSyncWithRetry()` 增加重试
3. 最终失败的消息应持久化到数据库或死信队列

## 六、配置检查清单

- [x] `acks=all` - 等待所有副本确认
- [x] `enable.idempotence=true` - 启用幂等性
- [x] `retries=Integer.MAX_VALUE` - 无限重试
- [x] `retry.backoff.ms=100` - 重试间隔
- [x] `request.timeout.ms=30000` - 请求超时
- [x] `delivery.timeout.ms=120000` - 交付超时
- [x] 同步发送方法带超时控制
- [x] 异步发送方法带完整回调处理
- [x] 详细的日志记录
- [x] 异常处理完善

## 七、参考资源

- [Kafka Producer Configs](https://kafka.apache.org/documentation/#producerconfigs)
- [Kafka Exactly-Once Semantics](https://kafka.apache.org/documentation/#semantics)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
