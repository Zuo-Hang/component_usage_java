error id: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/controller/KafkaController.java:_empty_/KafkaProducerService#sendToPartition#
file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/controller/KafkaController.java
empty definition using pc, found symbol in pc: _empty_/KafkaProducerService#sendToPartition#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3645
uri: file://<WORKSPACE>/mq-kafka-example/src/main/java/com/example/kafka/controller/KafkaController.java
text:
```scala
package com.example.kafka.controller;

import com.example.kafka.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka操作控制器
 */
@Slf4j
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * 同步发送消息
     * POST /kafka/send/sync?topic=test-topic&key=key1&message=hello
     */
    @PostMapping("/send/sync")
    public Map<String, Object> sendSync(
            @RequestParam String topic,
            @RequestParam(required = false) String key,
            @RequestParam String message) {
        try {
            SendResult<String, String> result = kafkaProducerService.sendSync(topic, key, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("partition", result.getRecordMetadata().partition());
            response.put("offset", result.getRecordMetadata().offset());
            response.put("message", "消息发送成功");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 异步发送消息
     * POST /kafka/send/async?topic=test-topic&key=key1&message=hello
     */
    @PostMapping("/send/async")
    public Map<String, Object> sendAsync(
            @RequestParam String topic,
            @RequestParam(required = false) String key,
            @RequestParam String message) {
        try {
            kafkaProducerService.sendAsync(topic, key, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("message", "消息已提交发送（异步）");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 发送消息（无键）
     * POST /kafka/send?topic=test-topic&message=hello
     */
    @PostMapping("/send")
    public Map<String, Object> send(
            @RequestParam String topic,
            @RequestParam String message) {
        try {
            kafkaProducerService.send(topic, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("message", "消息已提交发送（异步）");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 发送消息到指定分区
     * POST /kafka/send/partition?topic=test-topic&partition=0&key=key1&message=hello
     */
    @PostMapping("/send/partition")
    public Map<String, Object> sendToPartition(
            @RequestParam String topic,
            @RequestParam int partition,
            @RequestParam(required = false) String key,
            @RequestParam String message) {
        try {
            kafkaProducerService.sendTo@@Partition(topic, partition, key, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("partition", partition);
            response.put("message", "消息已提交发送到指定分区（异步）");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            return response;
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/KafkaProducerService#sendToPartition#