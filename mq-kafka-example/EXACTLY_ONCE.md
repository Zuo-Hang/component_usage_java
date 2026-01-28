# Kafka 精准一次性（Exactly-Once Semantics）说明

本模块已提供 **Kafka 官方精准一次性** 的完整展示：事务生产者 + `read_committed` 消费者。

## 与现有“幂等”的区别

| 能力 | 说明 | 本模块 |
|------|------|--------|
| **生产者幂等** | `enable.idempotence=true`，重试不重复落盘 | ✅ 已有 |
| **消费侧去重** | 手动 ack + 业务幂等/去重（如 processedMessageIds） | ✅ 已有 |
| **精准一次性（EOS）** | 事务 Producer + 消费者 `read_committed`，端到端不丢不重 | ✅ 本节展示 |

前两者合起来是“至少一次 + 应用层去重”；真正的 **Exactly-Once** 依赖 **事务 + read_committed**。

## 实现要点

### 1. 事务型生产者

- 使用单独的 `ProducerFactory`，并设置 `transactionIdPrefix`（如 `eos-tx-`）。
- 使用 `KafkaTemplate.executeInTransaction(...)` 在事务内发送。
- 事务提交后，消息才对使用 `read_committed` 的消费者可见；事务回滚则不会被读到。

### 2. 消费者 read_committed

- 消费者配置：`isolation.level=read_committed`。
- 只消费已提交事务的消息，未提交或已回滚的消息不会出现在消费中。
- 与上面的事务发送配合，实现 **端到端精准一次性** 展示。

### 3. 专用 Topic

- 示例使用 **`eos-topic`** 作为精准一次性演示的 topic。
- 生产环境可为 EOS 流量单独建 topic，便于统一使用 `read_committed` 消费。

## 代码与 API

### 生产者

- **事务内单条发送**：`KafkaProducerService.sendExactlyOnce(topic, key, message)`  
  - 内部使用 `transactionalKafkaTemplate.executeInTransaction(operations -> operations.send(...))`。
- **事务内批量发送**：`KafkaProducerService.sendExactlyOnceBatch(topic, messages)`  
  - 同一事务内发送多条，要么全部对消费者可见，要么全部不可见。

### 消费者

- **EOS 监听器**：`KafkaMessageConsumer.consumeEosTopic`  
  - 监听 `eos-topic`，使用 `readCommittedKafkaListenerContainerFactory`（即 `read_committed` + 手动 ack）。

### HTTP 接口

- **单条精准一次性发送**  
  `POST /kafka/send/eos?key=order-1&message=payload`  
  - 发送到 `eos-topic`，事务提交后仅对 read_committed 消费者可见。

- **批量精准一次性发送**  
  `POST /kafka/send/eos-batch`  
  - Body: `["msg1", "msg2", "msg3"]`  
  - 同一事务内发送到 `eos-topic`。

## 如何验证

1. 启动 Kafka（需支持事务，单机或集群均可）。
2. 创建 topic：`eos-topic`（如未自动创建）。
3. 启动本应用，调用：
   - `POST /kafka/send/eos?key=k1&message=test-eos`
4. 查看日志：消费者应收到且只收到一次该条消息（精准一次性消费）。
5. 若在 `executeInTransaction` 中抛异常，事务回滚，消费者不应收到该批消息。

## 注意

- **transactional.id**：同一 `transactionIdPrefix` 下，一个生产者实例对应一个逻辑事务 ID，多实例部署时需为每个实例配置不同前缀（如加 instanceId）。
- **Broker 配置**：生产环境需保证事务相关配置（如 `transaction.state.log.replication.factor`）符合要求。
- **EOS 的“一次”范围**：这里展示的是 **“从生产者事务提交到消费者 read_committed 读取”** 的一次性；若消费后再写库或再发到别的系统，端到端仍需结合业务幂等或下游事务。

本模块的精准一次性展示：**事务发送 + read_committed 消费**，可用于理解和演示 Kafka 官方的 Exactly-Once Semantics。
