error id: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/service/KafkaProducerService.java:_empty_/KafkaTemplate#
file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/service/KafkaProducerService.java
empty definition using pc, found symbol in pc: _empty_/KafkaTemplate#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 450
uri: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/service/KafkaProducerService.java
text:
```scala
package com.example.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka生产者服务
 */
@Slf4j
@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate@@<String, String> kafkaTemplate;

    /**
     * 同步发送消息
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @return 发送结果
     */
    public SendResult<String, String> sendSync(String topic, String key, String message) {
        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
            log.info("同步发送消息成功: topic={}, key={}, message={}, offset={}", 
                    topic, key, message, result.getRecordMetadata().offset());
            return result;
        } catch (Exception e) {
            log.error("同步发送消息失败: topic={}, key={}, message={}", topic, key, message, e);
            throw new RuntimeException("同步发送消息失败", e);
        }
    }

    /**
     * 异步发送消息
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     */
    public void sendAsync(String topic, String key, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("异步发送消息成功: topic={}, key={}, message={}, offset={}", 
                        topic, key, message, result.getRecordMetadata().offset());
            } else {
                log.error("异步发送消息失败: topic={}, key={}, message={}", topic, key, message, exception);
            }
        });
    }

    /**
     * 发送消息（无键）
     * @param topic 主题
     * @param message 消息内容
     */
    public void send(String topic, String message) {
        sendAsync(topic, null, message);
    }

    /**
     * 发送消息到指定分区
     * @param topic 主题
     * @param partition 分区号
     * @param key 消息键
     * @param message 消息内容
     */
    public void sendToPartition(String topic, int partition, String key, String message) {
        CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, partition, key, message);
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("发送消息到分区成功: topic={}, partition={}, key={}, message={}, offset={}", 
                        topic, partition, key, message, result.getRecordMetadata().offset());
            } else {
                log.error("发送消息到分区失败: topic={}, partition={}, key={}, message={}", 
                        topic, partition, key, message, exception);
            }
        });
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/KafkaTemplate#