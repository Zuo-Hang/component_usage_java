package com.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * 生产者配置（高可靠性）
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // ========== 可靠性保障配置 ==========
        // 1. 确认机制：等待所有副本（ISR）确认，最高可靠性
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // 2. 幂等性：防止重复消息（启用后自动设置 max.in.flight.requests.per.connection=5）
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // 3. 重试配置
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // 无限重试（配合幂等性使用）
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // 重试间隔100ms
        
        // 4. 请求超时配置
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 请求超时30秒
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 总交付超时2分钟
        
        // 5. 批量发送配置（提高吞吐量，同时保证可靠性）
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB批量大小
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // 等待10ms以批量发送
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB缓冲区
        
        // 6. 压缩配置（可选，提高网络传输效率）
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // 7. 最大飞行请求数（启用幂等性后自动为5，确保顺序）
        // configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * 事务型生产者工厂（用于精准一次性 Exactly-Once Semantics）
     * 必须设置 transactionIdPrefix，且每个 producer 实例唯一
     */
    @Bean
    public ProducerFactory<String, String> transactionalProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        DefaultKafkaProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(configProps);
        // 设置事务 ID 前缀（同一应用内唯一即可，多实例需加 instanceId）
        factory.setTransactionIdPrefix("eos-tx-");
        return factory;
    }

    /**
     * KafkaTemplate（普通，非事务）
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 事务型 KafkaTemplate（用于精准一次性发送）
     */
    @Bean("transactionalKafkaTemplate")
    public KafkaTemplate<String, String> transactionalKafkaTemplate() {
        return new KafkaTemplate<>(transactionalProducerFactory());
    }

    /**
     * 消费者配置（自动确认 - 仅用于非关键业务）
     * 注意：自动确认可能导致消息丢失，不推荐用于关键业务
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // 自动提交偏移量（不推荐用于关键业务）
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        // 从最早的消息开始消费（新消费者组）
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // 会话和心跳配置
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30秒
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3秒
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 消费者配置（手动确认 - 高可靠性）
     * 推荐用于关键业务，确保消息不丢失
     */
    @Bean
    public ConsumerFactory<String, String> manualAckConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-ack-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // ========== 可靠性保障配置 ==========
        // 1. 手动提交偏移量（确保消息处理成功后才提交）
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        // 2. 偏移量重置策略
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // 3. 会话和心跳配置（防止误判为离线）
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30秒会话超时
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3秒心跳间隔
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5分钟最大拉取间隔
        
        // 4. 每次拉取的消息数量（控制批处理大小）
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // 每次最多拉取10条
        
        // 5. 隔离级别（读取已提交的消息，配合事务使用）
        // props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 消费者配置（read_committed - 仅读取已提交事务的消息，用于精准一次性消费）
     */
    @Bean
    public ConsumerFactory<String, String> readCommittedConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "eos-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // 精准一次性：只消费已提交事务的消息，未提交/回滚的消息不会读到
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 消费者配置（顺序消费 - 单线程）
     * 确保分区内消息顺序处理
     */
    @Bean
    public ConsumerFactory<String, String> sequentialConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sequential-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // 手动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // 顺序消费关键配置：每次只拉取1条消息
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        
        // 会话配置
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 监听器容器工厂（自动确认）
     * 注意：仅用于非关键业务
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 设置并发数（每个分区一个线程）
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * 监听器容器工厂（手动确认 - 高可靠性）
     * 推荐用于关键业务
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> manualAckKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(manualAckConsumerFactory());
        
        // 设置手动确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 设置并发数（每个分区一个线程，保证分区内顺序）
        factory.setConcurrency(3);
        
        // 错误处理：设置错误处理器
        // 使用 DefaultErrorHandler，失败后重试3次，每次间隔1秒
        CommonErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, 3L) // 重试间隔1秒，最多重试3次
        );
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }

    /**
     * 监听器容器工厂（顺序消费 - 单线程）
     * 确保分区内消息严格顺序处理
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> sequentialKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(sequentialConsumerFactory());
        
        // 手动确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 关键：设置并发数为1，确保顺序处理
        factory.setConcurrency(1);
        
        return factory;
    }

    /**
     * 监听器容器工厂（read_committed - 精准一次性消费）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> readCommittedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(readCommittedConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(2);
        return factory;
    }
}
