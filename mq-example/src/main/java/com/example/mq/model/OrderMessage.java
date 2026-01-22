package com.example.mq.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单消息实体
 */
@Data
public class OrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private Long createTime;
}

