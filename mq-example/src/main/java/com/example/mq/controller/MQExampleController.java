package com.example.mq.controller;

import com.example.mq.model.OrderMessage;
import com.example.mq.service.MQProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息队列示例控制器
 */
@Slf4j
@RestController
@RequestMapping("/mq")
public class MQExampleController {

    @Autowired
    private MQProducerService mqProducerService;

    /**
     * 发送同步消息
     */
    @PostMapping("/sync")
    public String sendSyncMessage(@RequestParam Long orderId) {
        OrderMessage order = mqProducerService.createTestOrder(orderId);
        mqProducerService.sendSyncMessage(order);
        return "同步消息发送成功: orderId=" + orderId;
    }

    /**
     * 发送异步消息
     */
    @PostMapping("/async")
    public String sendAsyncMessage(@RequestParam Long orderId) {
        OrderMessage order = mqProducerService.createTestOrder(orderId);
        mqProducerService.sendAsyncMessage(order);
        return "异步消息发送成功: orderId=" + orderId;
    }

    /**
     * 发送单向消息
     */
    @PostMapping("/oneway")
    public String sendOneWayMessage(@RequestParam Long orderId) {
        OrderMessage order = mqProducerService.createTestOrder(orderId);
        mqProducerService.sendOneWayMessage(order);
        return "单向消息发送成功: orderId=" + orderId;
    }

    /**
     * 发送延迟消息
     */
    @PostMapping("/delay")
    public String sendDelayMessage(@RequestParam Long orderId, 
                                   @RequestParam(defaultValue = "3") int delayLevel) {
        OrderMessage order = mqProducerService.createTestOrder(orderId);
        mqProducerService.sendDelayMessage(order, delayLevel);
        return "延迟消息发送成功: orderId=" + orderId + ", delayLevel=" + delayLevel;
    }

    /**
     * 发送顺序消息
     */
    @PostMapping("/orderly")
    public String sendOrderlyMessage(@RequestParam Long orderId) {
        OrderMessage order = mqProducerService.createTestOrder(orderId);
        mqProducerService.sendOrderlyMessage(order);
        return "顺序消息发送成功: orderId=" + orderId;
    }

    /**
     * 发送批量消息
     */
    @PostMapping("/batch")
    public String sendBatchMessage() {
        mqProducerService.sendBatchMessage();
        return "批量消息发送成功";
    }
}

