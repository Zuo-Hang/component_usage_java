package com.example.mq.service;

import com.example.mq.model.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 消息生产者服务类
 * 演示RocketMQ消息发送的各种方式
 */
@Slf4j
@Service
public class MQProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 主题名称
    private static final String ORDER_TOPIC = "order-topic";
    private static final String ORDER_TAG = "order-create";

    /**
     * 发送同步消息
     * 同步消息会在发送成功后返回结果，适用于重要消息
     */
    public void sendSyncMessage(OrderMessage order) {
        try {
            rocketMQTemplate.syncSend(ORDER_TOPIC + ":" + ORDER_TAG, order);
            log.info("发送同步消息成功: orderId={}", order.getOrderId());
        } catch (Exception e) {
            log.error("发送同步消息失败", e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 发送异步消息
     * 异步消息发送后立即返回，不等待结果，适用于性能要求高的场景
     */
    public void sendAsyncMessage(OrderMessage order) {
        rocketMQTemplate.asyncSend(
            ORDER_TOPIC + ":" + ORDER_TAG,
            order,
            new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                    log.info("异步消息发送成功: orderId={}, msgId={}", 
                            order.getOrderId(), sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("异步消息发送失败: orderId={}", order.getOrderId(), e);
                }
            }
        );
    }

    /**
     * 发送单向消息
     * 单向消息只发送不等待结果，适用于日志等对可靠性要求不高的场景
     */
    public void sendOneWayMessage(OrderMessage order) {
        rocketMQTemplate.sendOneWay(ORDER_TOPIC + ":" + ORDER_TAG, order);
        log.info("发送单向消息: orderId={}", order.getOrderId());
    }

    /**
     * 发送延迟消息
     * @param order 订单消息
     * @param delayLevel 延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    public void sendDelayMessage(OrderMessage order, int delayLevel) {
        rocketMQTemplate.syncSend(
            ORDER_TOPIC + ":" + ORDER_TAG,
            MessageBuilder.withPayload(order).build(),
            3000,
            delayLevel
        );
        log.info("发送延迟消息: orderId={}, delayLevel={}", order.getOrderId(), delayLevel);
    }

    /**
     * 发送顺序消息
     * 保证同一个订单的消息按顺序处理
     */
    public void sendOrderlyMessage(OrderMessage order) {
        String hashKey = String.valueOf(order.getUserId());
        rocketMQTemplate.syncSendOrderly(
            ORDER_TOPIC + ":" + ORDER_TAG,
            order,
            hashKey
        );
        log.info("发送顺序消息: orderId={}, userId={}", order.getOrderId(), order.getUserId());
    }

    /**
     * 发送批量消息
     */
    public void sendBatchMessage() {
        for (int i = 1; i <= 10; i++) {
            OrderMessage order = new OrderMessage();
            order.setOrderId((long) i);
            order.setOrderNo("ORD" + i);
            order.setUserId(1001L);
            order.setAmount(new BigDecimal("100.00"));
            order.setStatus("PENDING");
            order.setCreateTime(System.currentTimeMillis());
            
            sendSyncMessage(order);
        }
        log.info("批量发送10条消息完成");
    }

    /**
     * 创建测试订单消息
     */
    public OrderMessage createTestOrder(Long orderId) {
        OrderMessage order = new OrderMessage();
        order.setOrderId(orderId);
        order.setOrderNo("ORD" + orderId);
        order.setUserId(1001L);
        order.setAmount(new BigDecimal("199.99"));
        order.setStatus("CREATED");
        order.setCreateTime(System.currentTimeMillis());
        return order;
    }
}

