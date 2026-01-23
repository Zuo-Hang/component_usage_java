error id: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/consumer/KafkaMessageConsumer.java:_empty_/KafkaListener#
file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/consumer/KafkaMessageConsumer.java
empty definition using pc, found symbol in pc: _empty_/KafkaListener#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 634
uri: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/consumer/KafkaMessageConsumer.java
text:
```scala
package com.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka消息消费者
 */
@Slf4j
@Component
public class KafkaMessageConsumer {

    /**
     * 监听 test-topic 主题的消息
     * @param message 消息内容
     * @param partition 分区号
     * @param offset 偏移量
     */
    @KafkaLis@@tener(topics = "test-topic", groupId = "test-group")
    public void consumeTestTopic(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("收到消息: topic=test-topic, partition={}, offset={}, message={}", 
                partition, offset, message);
    }

    /**
     * 监听 order-topic 主题的消息（手动确认）
     * @param message 消息内容
     * @param acknowledgment 确认对象
     */
    @KafkaListener(topics = "order-topic", groupId = "order-group", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderTopic(
            @Payload String message,
            Acknowledgment acknowledgment) {
        try {
            log.info("处理订单消息: message={}", message);
            // 业务处理逻辑
            // ...
            // 手动确认消息
            acknowledgment.acknowledge();
            log.info("订单消息处理完成并确认: message={}", message);
        } catch (Exception e) {
            log.error("处理订单消息失败: message={}", message, e);
            // 不确认，消息会重新消费
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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/KafkaListener#