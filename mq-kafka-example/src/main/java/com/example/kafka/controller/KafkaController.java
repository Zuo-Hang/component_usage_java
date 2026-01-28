package com.example.kafka.controller;

import com.example.kafka.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            kafkaProducerService.sendToPartition(topic, partition, key, message);
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

    /**
     * 带重试的同步发送（业务层重试）
     * POST /kafka/send/sync-retry?topic=test-topic&key=key1&message=hello&maxRetries=3
     */
    @PostMapping("/send/sync-retry")
    public Map<String, Object> sendSyncWithRetry(
            @RequestParam String topic,
            @RequestParam(required = false) String key,
            @RequestParam String message,
            @RequestParam(defaultValue = "3") int maxRetries) {
        try {
            SendResult<String, String> result = kafkaProducerService.sendSyncWithRetry(
                    topic, key, message, maxRetries);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("partition", result.getRecordMetadata().partition());
            response.put("offset", result.getRecordMetadata().offset());
            response.put("retries", maxRetries);
            response.put("message", "消息发送成功（带重试保障）");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "消息发送失败（已重试）: " + e.getMessage());
            return response;
        }
    }

    /**
     * 精准一次性发送（Exactly-Once Semantics）
     * 使用事务发送到 eos-topic，消费者需使用 read_committed 才会读到
     * 
     * POST /kafka/send/eos?key=order-1&message=payload
     */
    @PostMapping("/send/eos")
    public Map<String, Object> sendExactlyOnce(
            @RequestParam(required = false) String key,
            @RequestParam String message) {
        String topic = "eos-topic";
        try {
            SendResult<String, String> result = kafkaProducerService.sendExactlyOnce(topic, key, message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("partition", result.getRecordMetadata().partition());
            response.put("offset", result.getRecordMetadata().offset());
            response.put("semantics", "exactly-once");
            response.put("message", "消息已以事务方式发送（精准一次性），仅 read_committed 消费者可见");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "精准一次性发送失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 精准一次性批量发送
     * POST /kafka/send/eos-batch
     * Body: ["msg1", "msg2", "msg3"]
     */
    @PostMapping("/send/eos-batch")
    public Map<String, Object> sendExactlyOnceBatch(@RequestBody List<String> messages) {
        String topic = "eos-topic";
        try {
            kafkaProducerService.sendExactlyOnceBatch(topic, messages);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("topic", topic);
            response.put("count", messages.size());
            response.put("semantics", "exactly-once");
            response.put("message", "批量消息已在同一事务内发送，要么全部可见要么全部不可见");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "精准一次性批量发送失败: " + e.getMessage());
            return response;
        }
    }
}
