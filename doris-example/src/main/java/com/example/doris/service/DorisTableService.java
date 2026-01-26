package com.example.doris.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Doris表管理服务
 * 
 * 生产环境要点：
 * 1. 表模型选择（Unique/Duplicate/Aggregate）
 * 2. 分区和分桶策略
 * 3. 物化视图设计
 * 4. 索引优化
 */
@Slf4j
@Service
public class DorisTableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建用户表（Duplicate模型）
     * 
     * Duplicate模型特点：
     * - 适合明细数据存储
     * - 不进行预聚合
     * - 保留所有数据
     */
    public void createUserTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT NOT NULL,
                username VARCHAR(50),
                email VARCHAR(100),
                city VARCHAR(50),
                country VARCHAR(50),
                register_time DATETIME,
                age INT,
                gender VARCHAR(10)
            ) ENGINE=OLAP
            DUPLICATE KEY(user_id)
            PARTITION BY RANGE(register_time) (
                PARTITION p202401 VALUES [("2024-01-01"), ("2024-02-01")),
                PARTITION p202402 VALUES [("2024-02-01"), ("2024-03-01")),
                PARTITION p202403 VALUES [("2024-03-01"), ("2024-04-01"))
            )
            DISTRIBUTED BY HASH(user_id) BUCKETS 10
            PROPERTIES (
                "replication_num" = "1",
                "storage_medium" = "SSD"
            )
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("用户表创建成功（Duplicate模型）");
        } catch (Exception e) {
            log.error("创建用户表失败", e);
            throw new RuntimeException("创建用户表失败", e);
        }
    }

    /**
     * 创建订单表（Unique模型）
     * 
     * Unique模型特点：
     * - 支持主键唯一性约束
     * - 支持实时更新（UPDATE/DELETE）
     * - 适合需要更新的场景
     * 
     * 这是Doris相比ClickHouse的核心优势之一
     */
    public void createOrderTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS orders (
                order_id BIGINT NOT NULL,
                user_id BIGINT,
                product_name VARCHAR(100),
                amount DECIMAL(10, 2),
                status VARCHAR(20),
                order_time DATETIME,
                update_time DATETIME
            ) ENGINE=OLAP
            UNIQUE KEY(order_id)
            PARTITION BY RANGE(order_time) (
                PARTITION p202401 VALUES [("2024-01-01"), ("2024-02-01")),
                PARTITION p202402 VALUES [("2024-02-01"), ("2024-03-01")),
                PARTITION p202403 VALUES [("2024-03-01"), ("2024-04-01"))
            )
            DISTRIBUTED BY HASH(order_id) BUCKETS 10
            PROPERTIES (
                "replication_num" = "1",
                "enable_unique_key_merge_on_write" = "true"
            )
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("订单表创建成功（Unique模型，支持实时更新）");
        } catch (Exception e) {
            log.error("创建订单表失败", e);
            throw new RuntimeException("创建订单表失败", e);
        }
    }

    /**
     * 创建订单汇总表（Aggregate模型）
     * 
     * Aggregate模型特点：
     * - 自动预聚合
     * - 适合统计场景
     * - 减少存储空间
     */
    public void createOrderSummaryTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS order_summary (
                user_id BIGINT,
                order_date DATE,
                total_orders BIGINT SUM,
                total_amount DECIMAL(10, 2) SUM,
                avg_amount DECIMAL(10, 2) REPLACE_IF_NOT_NULL
            ) ENGINE=OLAP
            AGGREGATE KEY(user_id, order_date)
            PARTITION BY RANGE(order_date) (
                PARTITION p202401 VALUES [("2024-01-01"), ("2024-02-01")),
                PARTITION p202402 VALUES [("2024-02-01"), ("2024-03-01")),
                PARTITION p202403 VALUES [("2024-03-01"), ("2024-04-01"))
            )
            DISTRIBUTED BY HASH(user_id) BUCKETS 10
            PROPERTIES (
                "replication_num" = "1"
            )
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("订单汇总表创建成功（Aggregate模型，自动预聚合）");
        } catch (Exception e) {
            log.error("创建订单汇总表失败", e);
            throw new RuntimeException("创建订单汇总表失败", e);
        }
    }

    /**
     * 创建物化视图（多表物化视图）
     * 
     * Doris物化视图优势：
     * - 支持多表物化视图
     * - 自动查询重写
     * - 提高复杂JOIN查询性能
     */
    public void createMaterializedView() {
        String sql = """
            CREATE MATERIALIZED VIEW IF NOT EXISTS user_order_mv
            AS
            SELECT
                u.user_id,
                u.username,
                u.city,
                u.country,
                DATE(o.order_time) AS order_date,
                COUNT(o.order_id) AS order_count,
                SUM(o.amount) AS total_amount,
                AVG(o.amount) AS avg_amount
            FROM users u
            LEFT JOIN orders o ON u.user_id = o.user_id
            GROUP BY u.user_id, u.username, u.city, u.country, DATE(o.order_time)
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("物化视图创建成功（多表物化视图，支持查询重写）");
        } catch (Exception e) {
            log.error("创建物化视图失败", e);
            throw new RuntimeException("创建物化视图失败", e);
        }
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
            SELECT COUNT(*) 
            FROM information_schema.tables 
            WHERE table_schema = DATABASE() 
            AND table_name = ?
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
                table_name,
                engine,
                table_model,
                partition_info,
                distribution_info
            FROM information_schema.tables 
            WHERE table_schema = DATABASE() 
            AND table_name = ?
            """;
        try {
            jdbcTemplate.query(sql, rs -> {
                log.info("表信息 - name: {}, engine: {}, model: {}",
                        rs.getString("table_name"),
                        rs.getString("engine"),
                        rs.getString("table_model"));
            }, tableName);
        } catch (Exception e) {
            log.error("获取表信息失败: {}", tableName, e);
        }
    }
}
