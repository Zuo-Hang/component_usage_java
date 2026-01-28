package com.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka消息消费者（高可靠性版本）
 * 
 * 关键点：
 * 1. 消费不丢：使用手动确认，处理成功后才提交offset
 * 2. 消费不重：使用幂等性检查（基于消息ID或业务唯一标识）
 * 3. 顺序消费：单线程处理或按key分区
 */
@Slf4j
@Component
public class KafkaMessageConsumer {

    // 幂等性检查：已处理的消息ID集合（生产环境应使用Redis或数据库）
    // 注意：这里使用内存存储仅用于演示，生产环境应使用持久化存储
    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();

    /**
     * 监听 test-topic 主题的消息（自动确认 - 仅用于非关键业务）
     * 
     * @param message 消息内容
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consumeTestTopic(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("收到消息: topic=test-topic, partition={}, offset={}, message={}", 
                partition, offset, message);
        // 注意：自动确认模式下，如果这里抛出异常，消息可能丢失
    }

    /**
     * 监听 order-topic 主题的消息（手动确认 - 高可靠性）
     * 
     * 关键点：
     * 1. 使用手动确认，确保消息处理成功后才提交offset
     * 2. 添加幂等性检查，防止重复消费
     * 3. 异常处理：处理失败时不确认，消息会重新消费
     * 
     * @param message 消息内容
     * @param acknowledgment 确认对象
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(topics = "order-topic", groupId = "order-group", 
                   containerFactory = "manualAckKafkaListenerContainerFactory")
    public void consumeOrderTopic(
            @Payload String message,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        // 生成消息唯一标识（生产环境应从消息中提取业务ID）
        String messageId = String.format("%s-%d-%d", "order-topic", partition, offset);
        
        try {
            // ========== 1. 幂等性检查：防止重复消费 ==========
            if (processedMessageIds.contains(messageId)) {
                log.warn("消息已处理过，跳过重复消费: messageId={}, message={}", messageId, message);
                // 确认消息（避免重复处理，但已处理过）
                acknowledgment.acknowledge();
                return;
            }
            
            log.info("开始处理订单消息: messageId={}, partition={}, offset={}, message={}", 
                    messageId, partition, offset, message);
            
            // ========== 2. 业务处理逻辑 ==========
            // 模拟业务处理
            processOrderMessage(message);
            
            // ========== 3. 记录已处理的消息ID（幂等性保障）==========
            processedMessageIds.add(messageId);
            
            // ========== 4. 手动确认消息（只有处理成功才确认）==========
            acknowledgment.acknowledge();
            
            log.info("订单消息处理完成并确认: messageId={}, message={}", messageId, message);
            
        } catch (Exception e) {
            log.error("处理订单消息失败: messageId={}, message={}, error={}", 
                    messageId, message, e.getMessage(), e);
            
            // ========== 5. 处理失败时不确认，消息会重新消费 ==========
            // 注意：这里不调用 acknowledgment.acknowledge()
            // Kafka 会在下次拉取时重新消费这条消息
            
            // 可选：如果重试多次仍失败，可以发送到死信队列
            // sendToDeadLetterQueue(messageId, message, e);
            
            // 重新抛出异常，让错误处理器处理
            throw new RuntimeException("处理订单消息失败", e);
        }
    }

    /**
     * 顺序消费示例（单线程处理，确保分区内顺序）
     * 
     * 关键点：
     * 1. 使用 sequentialKafkaListenerContainerFactory（并发数为1）
     * 2. 每次只拉取1条消息（MAX_POLL_RECORDS=1）
     * 3. 处理完一条再处理下一条，保证顺序
     * 
     * @param message 消息内容
     * @param acknowledgment 确认对象
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaListener(topics = "sequential-topic", groupId = "sequential-group",
                   containerFactory = "sequentialKafkaListenerContainerFactory")
    public void consumeSequential(
            @Payload String message,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        String messageId = String.format("%s-%d-%d", "sequential-topic", partition, offset);
        
        try {
            log.info("顺序处理消息: messageId={}, partition={}, offset={}, message={}", 
                    messageId, partition, offset, message);
            
            // 业务处理（按顺序执行）
            processSequentialMessage(message);
            
            // 幂等性检查
            if (!processedMessageIds.contains(messageId)) {
                processedMessageIds.add(messageId);
            }
            
            // 确认消息
            acknowledgment.acknowledge();
            
            log.info("顺序消息处理完成: messageId={}", messageId);
            
        } catch (Exception e) {
            log.error("顺序消息处理失败: messageId={}, message={}", messageId, message, e);
            // 不确认，重新消费
            throw new RuntimeException("顺序消息处理失败", e);
        }
    }

    /**
     * 精准一次性消费示例（Exactly-Once Semantics）
     * 
     * 使用 read_committed 隔离级别：
     * - 只读取已提交事务的消息
     * - 未提交或已回滚的事务消息不会出现在消费中
     * 配合生产者 sendExactlyOnce() 使用，实现端到端精准一次性
     */
    @KafkaListener(topics = "eos-topic", groupId = "eos-consumer-group",
                   containerFactory = "readCommittedKafkaListenerContainerFactory")
    public void consumeEosTopic(
            @Payload String message,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        String messageId = String.format("eos-topic-%d-%d", partition, offset);
        try {
            log.info("精准一次性消费: messageId={}, key={}, partition={}, offset={}, message={}",
                    messageId, key, partition, offset, message);
            // 业务处理（每条消息仅被处理一次，且不会读到未提交事务的消息）
            processOrderMessage(message);
            if (!processedMessageIds.contains(messageId)) {
                processedMessageIds.add(messageId);
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("精准一次性消费处理失败: messageId={}, message={}", messageId, message, e);
            throw new RuntimeException("EOS消费处理失败", e);
        }
    }

    /**
     * 监听多个主题
     * @param message 消息内容
     * @param topic 主题名称
     */
    @KafkaListener(topics = {"topic1", "topic2"}, groupId = "multi-topic-group")
    public void consumeMultipleTopics(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("收到消息: topic={}, message={}", topic, message);
    }

    /**
     * 按key分区的顺序消费示例
     * 
     * 关键点：
     * 1. 相同key的消息会发送到同一分区
     * 2. 同一分区内的消息是顺序的
     * 3. 不同分区的消息可以并行处理
     * 
     * @param message 消息内容
     * @param key 消息key
     * @param acknowledgment 确认对象
     * @param partition 分区号
     */
    @KafkaListener(topics = "key-ordered-topic", groupId = "key-ordered-group",
                   containerFactory = "manualAckKafkaListenerContainerFactory")
    public void consumeKeyOrdered(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        
        try {
            log.info("按key顺序处理消息: key={}, partition={}, message={}", key, partition, message);
            
            // 业务处理（相同key的消息会按顺序处理）
            processKeyOrderedMessage(key, message);
            
            // 确认消息
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("按key顺序消息处理失败: key={}, message={}", key, message, e);
            throw new RuntimeException("按key顺序消息处理失败", e);
        }
    }

    // ========== 业务处理方法 ==========

    /**
     * 处理订单消息
     */
    private void processOrderMessage(String message) {
        // 模拟业务处理
        // 1. 解析消息
        // 2. 验证数据
        // 3. 保存到数据库
        // 4. 发送通知等
        
        // 模拟处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 模拟处理失败（用于测试）
        // if (message.contains("error")) {
        //     throw new RuntimeException("模拟处理失败");
        // }
    }

    /**
     * 处理顺序消息
     */
    private void processSequentialMessage(String message) {
        // 顺序处理逻辑
        log.debug("处理顺序消息: {}", message);
    }

    /**
     * 处理按key顺序的消息
     */
    private void processKeyOrderedMessage(String key, String message) {
        // 按key顺序处理逻辑
        log.debug("处理按key顺序的消息: key={}, message={}", key, message);
    }

    /**
     * 发送到死信队列（可选）
     */
    private void sendToDeadLetterQueue(String messageId, String message, Exception error) {
        // 实现死信队列逻辑
        // 1. 记录失败消息到数据库
        // 2. 发送到死信Topic
        // 3. 发送告警通知
        log.error("发送到死信队列: messageId={}, message={}, error={}", 
                messageId, message, error.getMessage());
    }
}
