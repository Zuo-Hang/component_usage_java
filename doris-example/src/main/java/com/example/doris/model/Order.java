package com.example.doris.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 
 * 用于演示多表JOIN场景和实时更新功能
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long orderId;
    private Long userId;
    private String productName;
    private BigDecimal amount;
    private String status;  // pending, paid, shipped, completed, cancelled
    private LocalDateTime orderTime;
    private LocalDateTime updateTime;
}
