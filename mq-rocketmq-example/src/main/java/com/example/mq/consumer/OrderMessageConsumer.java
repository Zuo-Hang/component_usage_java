package com.example.mq.consumer;

import com.example.mq.model.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 订单消息消费者
 * 监听订单主题的消息并处理
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = "order-topic",
        selectorExpression = "order-create",
        consumerGroup = "order-consumer-group",
        nameServer = "${rocketmq.name-server}"
)
public class OrderMessageConsumer implements RocketMQListener<OrderMessage> {

    @Override
    public void onMessage(OrderMessage order) {
        log.info("接收到订单消息: orderId={}, orderNo={}, userId={}, amount={}, status={}",
                order.getOrderId(), order.getOrderNo(), order.getUserId(),
                order.getAmount(), order.getStatus());

        try {
            // 模拟业务处理
            processOrder(order);
            log.info("订单处理成功: orderId={}", order.getOrderId());
        } catch (Exception e) {
            log.error("订单处理失败: orderId={}", order.getOrderId(), e);
            // 在实际应用中，这里应该进行重试或者将消息发送到死信队列
            throw new RuntimeException("订单处理失败", e);
        }
    }

    /**
     * 处理订单业务逻辑
     */
    private void processOrder(OrderMessage order) {
        // 模拟业务处理：库存扣减、支付处理等
        log.info("开始处理订单: orderId={}", order.getOrderId());
        
        // 这里可以调用订单服务、库存服务等
        // orderService.process(order);
        
        log.info("订单处理完成: orderId={}", order.getOrderId());
    }
}

