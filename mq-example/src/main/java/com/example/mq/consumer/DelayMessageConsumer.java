package com.example.mq.consumer;

import com.example.mq.model.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 延迟消息消费者
 * 用于处理订单超时等延迟场景
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = "order-topic",
        selectorExpression = "order-timeout",
        consumerGroup = "delay-order-consumer-group"
)
public class DelayMessageConsumer implements RocketMQListener<OrderMessage> {

    @Override
    public void onMessage(OrderMessage order) {
        log.info("接收到延迟消息: orderId={}, orderNo={}", 
                order.getOrderId(), order.getOrderNo());
        
        // 检查订单状态，如果仍为待支付，则取消订单
        checkAndCancelOrder(order);
    }

    /**
     * 检查并取消超时订单
     */
    private void checkAndCancelOrder(OrderMessage order) {
        log.info("检查订单状态: orderId={}", order.getOrderId());
        
        // 模拟检查订单状态
        // if (orderService.getOrderStatus(order.getOrderId()).equals("PENDING")) {
        //     orderService.cancelOrder(order.getOrderId());
        //     log.info("订单已超时，自动取消: orderId={}", order.getOrderId());
        // }
        
        log.info("订单检查完成: orderId={}", order.getOrderId());
    }
}

