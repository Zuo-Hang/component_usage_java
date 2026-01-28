package com.example.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka生产者服务（高可靠性版本）
 */
@Slf4j
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("transactionalKafkaTemplate")
    private KafkaTemplate<String, String> transactionalKafkaTemplate;

    /**
     * 同步发送消息（最高可靠性）
     * 等待消息确认后才返回，适合对可靠性要求极高的场景
     * 
     * @param topic 主题
     * @param key 消息键（可为null）
     * @param message 消息内容
     * @return 发送结果
     * @throws RuntimeException 发送失败时抛出异常
     */
    @SuppressWarnings("null")
    public SendResult<String, String> sendSync(String topic, @Nullable String key, String message) {
        try {
            // 使用 get() 等待结果，确保消息已成功发送并得到确认
            // Kafka 支持 null key，这里抑制类型检查警告
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
            SendResult<String, String> result = future.get(30, TimeUnit.SECONDS); // 设置30秒超时
            
            RecordMetadata metadata = result.getRecordMetadata();
            log.info("同步发送消息成功: topic={}, partition={}, offset={}, key={}, message={}", 
                    topic, metadata.partition(), metadata.offset(), key, message);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("同步发送消息被中断: topic={}, key={}, message={}", topic, key, message, e);
            throw new RuntimeException("同步发送消息被中断", e);
        } catch (ExecutionException e) {
            log.error("同步发送消息执行失败: topic={}, key={}, message={}", topic, key, message, e);
            throw new RuntimeException("同步发送消息执行失败: " + e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            log.error("同步发送消息超时: topic={}, key={}, message={}", topic, key, message, e);
            throw new RuntimeException("同步发送消息超时", e);
        }
    }

    /**
     * 异步发送消息（带完整回调处理）
     * 适合高吞吐量场景，通过回调处理成功/失败情况
     * 
     * @param topic 主题
     * @param key 消息键（可为null）
     * @param message 消息内容
     * @return CompletableFuture 用于链式处理
     */
    @SuppressWarnings("null")
    public CompletableFuture<SendResult<String, String>> sendAsync(String topic, @Nullable String key, String message) {
        try {
            // Kafka 支持 null key，这里抑制类型检查警告
            CompletableFuture<SendResult<String, String>> kafkaFuture = 
                    kafkaTemplate.send(topic, key, message);
            
            // 使用 CompletableFuture 的回调方法
            kafkaFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("异步发送消息成功: topic={}, partition={}, offset={}, key={}, message={}", 
                            topic, metadata.partition(), metadata.offset(), key, message);
                } else {
                    log.error("异步发送消息失败: topic={}, key={}, message={}, error={}", 
                            topic, key, message, ex.getMessage(), ex);
                    // 注意：Kafka会自动重试，这里记录的是最终失败的情况
                    // 可以根据业务需求决定是否需要持久化失败的消息到数据库或死信队列
                }
            });
            
            return kafkaFuture;
        } catch (Exception e) {
            log.error("提交异步发送任务失败: topic={}, key={}, message={}", topic, key, message, e);
            CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    /**
     * 发送消息（无键，异步）
     * @param topic 主题
     * @param message 消息内容
     */
    public void send(String topic, String message) {
        sendAsync(topic, null, message);
    }

    /**
     * 发送消息到指定分区（异步）
     * @param topic 主题
     * @param partition 分区号
     * @param key 消息键（可为null）
     * @param message 消息内容
     */
    public CompletableFuture<SendResult<String, String>> sendToPartition(
            String topic, int partition, @Nullable String key, String message) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, partition, key, message);
            CompletableFuture<SendResult<String, String>> kafkaFuture = kafkaTemplate.send(record);
            
            // 使用 CompletableFuture 的回调方法
            kafkaFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("发送消息到分区成功: topic={}, partition={}, offset={}, key={}, message={}", 
                            topic, partition, metadata.offset(), key, message);
                } else {
                    log.error("发送消息到分区失败: topic={}, partition={}, key={}, message={}, error={}", 
                            topic, partition, key, message, ex.getMessage(), ex);
                }
            });
            
            return kafkaFuture;
        } catch (Exception e) {
            log.error("提交分区发送任务失败: topic={}, partition={}, key={}, message={}", 
                    topic, partition, key, message, e);
            CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    /**
     * 带重试的同步发送（业务层重试）
     * 适用于网络抖动等临时性故障
     * 
     * @param topic 主题
     * @param key 消息键（可为null）
     * @param message 消息内容
     * @param maxRetries 最大重试次数
     * @return 发送结果
     */
    public SendResult<String, String> sendSyncWithRetry(
            String topic, @Nullable String key, String message, int maxRetries) {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < maxRetries) {
            try {
                return sendSync(topic, key, message);
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < maxRetries) {
                    log.warn("发送失败，进行第{}次重试: topic={}, key={}, error={}", 
                            attempts, topic, key, e.getMessage());
                    try {
                        Thread.sleep(100 * attempts); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        log.error("发送失败，已重试{}次: topic={}, key={}, message={}", 
                maxRetries, topic, key, message, lastException);
        throw new RuntimeException("发送失败，已重试" + maxRetries + "次", lastException);
    }

    /**
     * 精准一次性发送：在事务内发送消息（Exactly-Once Semantics）
     * 配合消费者 read_committed 隔离级别，可实现端到端精准一次性。
     * 
     * 行为：
     * - 事务提交成功：消息对 read_committed 消费者可见
     * - 事务回滚或未提交：消息不会对 read_committed 消费者可见
     * 
     * @param topic 主题（建议使用 eos-topic 等专用 topic）
     * @param key   消息 key（可为 null）
     * @param message 消息内容
     * @return 发送结果
     */
    @SuppressWarnings("null")
    public SendResult<String, String> sendExactlyOnce(String topic, @Nullable String key, String message) {
        return transactionalKafkaTemplate.executeInTransaction(operations -> {
            try {
                CompletableFuture<SendResult<String, String>> future = operations.send(topic, key, message);
                SendResult<String, String> result = future.get(30, TimeUnit.SECONDS);
                RecordMetadata meta = result.getRecordMetadata();
                log.info("事务内发送成功(精准一次性): topic={}, partition={}, offset={}, key={}", 
                        topic, meta.partition(), meta.offset(), key);
                return result;
            } catch (Exception e) {
                log.error("事务内发送失败，事务将回滚: topic={}, key={}, error={}", topic, key, e.getMessage());
                throw new RuntimeException("事务发送失败", e);
            }
        });
    }

    /**
     * 精准一次性批量发送：在同一个事务内发送多条消息，要么全成功要么全不可见
     */
    @SuppressWarnings("null")
    public void sendExactlyOnceBatch(String topic, List<String> messages) {
        transactionalKafkaTemplate.executeInTransaction(operations -> {
            for (int i = 0; i < messages.size(); i++) {
                String key = "eos-" + i;
                operations.send(topic, key, messages.get(i));
            }
            log.info("事务内批量发送: topic={}, count={}", topic, messages.size());
            return null;
        });
    }
}
