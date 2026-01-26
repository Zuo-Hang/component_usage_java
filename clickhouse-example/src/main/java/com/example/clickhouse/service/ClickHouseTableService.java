package com.example.clickhouse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * ClickHouse表管理服务
 * 
 * 生产环境要点：
 * 1. 表引擎选择（MergeTree系列）
 * 2. 分区策略（按时间分区）
 * 3. 排序键设计（ORDER BY）
 * 4. TTL设置（数据生命周期）
 * 5. 索引优化
 */
@Slf4j
@Service
public class ClickHouseTableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建用户行为日志表
     * 
     * 生产环境表设计要点：
     * - ENGINE: MergeTree（最常用）
     * - PARTITION BY: 按时间分区（按月），提高查询效率
     * - ORDER BY: 按查询常用字段排序（event_time, user_id）
     * - TTL: 设置数据保留时间（90天），自动清理旧数据
     */
    public void createUserBehaviorLogTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_behavior_log (
                event_time DateTime,
                user_id String,
                event_type String,
                page_url String,
                duration Int32,
                device_type String,
                ip_address String
            ) ENGINE = MergeTree()
            PARTITION BY toYYYYMM(event_time)
            ORDER BY (event_time, user_id)
            TTL event_time + INTERVAL 90 DAY
            SETTINGS index_granularity = 8192
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("用户行为日志表创建成功");
        } catch (Exception e) {
            log.error("创建表失败", e);
            throw new RuntimeException("创建表失败", e);
        }
    }

    /**
     * 创建物化视图（预聚合表）
     * 
     * 生产环境优化策略：
     * - 物化视图用于预聚合常用查询
     * - 减少实时计算压力
     * - 提高查询性能
     */
    public void createMaterializedView() {
        // 按小时统计用户行为
        String sql = """
            CREATE MATERIALIZED VIEW IF NOT EXISTS user_behavior_hourly_mv
            ENGINE = SummingMergeTree()
            PARTITION BY toYYYYMM(hour_time)
            ORDER BY (hour_time, event_type, device_type)
            AS SELECT
                toStartOfHour(event_time) AS hour_time,
                event_type,
                device_type,
                count() AS event_count,
                sum(duration) AS total_duration,
                uniqExact(user_id) AS unique_users
            FROM user_behavior_log
            GROUP BY hour_time, event_type, device_type
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("物化视图创建成功");
        } catch (Exception e) {
            log.error("创建物化视图失败", e);
            throw new RuntimeException("创建物化视图失败", e);
        }
    }

    /**
     * 创建分布式表（集群环境）
     * 
     * 注意：单机环境不需要，这里仅作演示
     */
    public void createDistributedTable() {
        // 分布式表需要集群环境，这里仅作示例
        log.info("分布式表需要集群环境，单机环境跳过");
    }

    /**
     * 删除表
     */
    public void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        try {
            jdbcTemplate.execute(sql);
            log.info("表删除成功: {}", tableName);
        } catch (Exception e) {
            log.error("删除表失败: {}", tableName, e);
            throw new RuntimeException("删除表失败", e);
        }
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        String sql = """
            SELECT count() 
            FROM system.tables 
            WHERE database = currentDatabase() 
            AND name = ?
            """;
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查表是否存在失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 获取表信息
     */
    public void showTableInfo(String tableName) {
        String sql = """
            SELECT 
                name,
                engine,
                partition_key,
                sorting_key,
                ttl
            FROM system.tables 
            WHERE database = currentDatabase() 
            AND name = ?
            """;
        try {
            jdbcTemplate.query(sql, rs -> {
                log.info("表信息 - name: {}, engine: {}, partition_key: {}, sorting_key: {}, ttl: {}",
                        rs.getString("name"),
                        rs.getString("engine"),
                        rs.getString("partition_key"),
                        rs.getString("sorting_key"),
                        rs.getString("ttl"));
            }, tableName);
        } catch (Exception e) {
            log.error("获取表信息失败: {}", tableName, e);
        }
    }
}
