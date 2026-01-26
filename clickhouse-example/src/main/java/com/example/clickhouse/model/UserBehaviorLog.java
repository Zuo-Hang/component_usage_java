package com.example.clickhouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户行为日志实体
 * 
 * 这是ClickHouse在生产环境中的典型使用场景：
 * - 时间序列数据（按时间分区）
 * - 大量写入（批量插入）
 * - 分析型查询（按时间、用户、行为类型聚合）
 * 
 * 对应表结构：
 * CREATE TABLE user_behavior_log (
 *     event_time DateTime,
 *     user_id String,
 *     event_type String,
 *     page_url String,
 *     duration Int32,
 *     device_type String,
 *     ip_address String
 * ) ENGINE = MergeTree()
 * PARTITION BY toYYYYMM(event_time)
 * ORDER BY (event_time, user_id)
 * TTL event_time + INTERVAL 90 DAY;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorLog {

    /**
     * 事件时间（用于分区和排序）
     */
    private LocalDateTime eventTime;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 事件类型（如：page_view, click, purchase等）
     */
    private String eventType;

    /**
     * 页面URL
     */
    private String pageUrl;

    /**
     * 停留时长（秒）
     */
    private Integer duration;

    /**
     * 设备类型（mobile, desktop, tablet）
     */
    private String deviceType;

    /**
     * IP地址
     */
    private String ipAddress;
}
