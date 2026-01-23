error id: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/config/KafkaConfig.java:_empty_/ProducerConfig#RETRIES_CONFIG#
file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/config/KafkaConfig.java
empty definition using pc, found symbol in pc: _empty_/ProducerConfig#RETRIES_CONFIG#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1803
uri: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/config/KafkaConfig.java
text:
```scala
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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

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
        configProps.put(ProducerConfig.RETRIE@@S_CONFIG, Integer.MAX_VALUE); // 无限重试（配合幂等性使用）
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
     * KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 消费者配置（自动确认）
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 自动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        // 从最早的消息开始消费
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 消费者配置（手动确认）
     */
    @Bean
    public ConsumerFactory<String, String> manualAckConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-ack-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 手动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 监听器容器工厂（自动确认）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 设置并发数
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * 监听器容器工厂（手动确认）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> manualAckKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(manualAckConsumerFactory());
        // 设置手动确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        return factory;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ProducerConfig#RETRIES_CONFIG#