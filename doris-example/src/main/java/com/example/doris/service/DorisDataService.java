package com.example.doris.service;

import com.example.doris.model.Order;
import com.example.doris.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Doris数据操作服务
 * 
 * 生产环境最佳实践：
 * 1. 批量插入（提高性能）
 * 2. 实时更新（Doris核心优势）
 * 3. 多表JOIN查询（CBO优化）
 * 4. 物化视图查询重写
 */
@Slf4j
@Service
public class DorisDataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== 用户表操作 ==========

    /**
     * 批量插入用户
     */
    public int batchInsertUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return 0;
        }

        String sql = """
            INSERT INTO users 
            (user_id, username, email, city, country, register_time, age, gender)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                User user = users.get(i);
                ps.setLong(1, user.getUserId());
                ps.setString(2, user.getUsername());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getCity());
                ps.setString(5, user.getCountry());
                ps.setString(6, user.getRegisterTime() != null ? 
                        user.getRegisterTime().format(DATE_TIME_FORMATTER) : null);
                ps.setObject(7, user.getAge());
                ps.setString(8, user.getGender());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });

        int totalInserted = results.length;
        log.info("批量插入用户成功: 数量={}", totalInserted);
        return totalInserted;
    }

    /**
     * 更新用户信息（Doris支持实时更新）
     * 
     * 这是Doris相比ClickHouse的核心优势之一
     */
    public int updateUser(Long userId, String email, String city) {
        String sql = """
            UPDATE users 
            SET email = ?, city = ?
            WHERE user_id = ?
            """;
        
        try {
            int count = jdbcTemplate.update(sql, email, city, userId);
            log.info("更新用户成功: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("更新用户失败: userId={}", userId, e);
            throw new RuntimeException("更新用户失败", e);
        }
    }

    /**
     * 删除用户（Doris支持实时删除）
     */
    public int deleteUser(Long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try {
            int count = jdbcTemplate.update(sql, userId);
            log.info("删除用户成功: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("删除用户失败: userId={}", userId, e);
            throw new RuntimeException("删除用户失败", e);
        }
    }

    // ========== 订单表操作 ==========

    /**
     * 批量插入订单
     */
    public int batchInsertOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return 0;
        }

        String sql = """
            INSERT INTO orders 
            (order_id, user_id, product_name, amount, status, order_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Order order = orders.get(i);
                ps.setLong(1, order.getOrderId());
                ps.setLong(2, order.getUserId());
                ps.setString(3, order.getProductName());
                ps.setBigDecimal(4, order.getAmount());
                ps.setString(5, order.getStatus());
                ps.setString(6, order.getOrderTime() != null ? 
                        order.getOrderTime().format(DATE_TIME_FORMATTER) : null);
                ps.setString(7, order.getUpdateTime() != null ? 
                        order.getUpdateTime().format(DATE_TIME_FORMATTER) : null);
            }

            @Override
            public int getBatchSize() {
                return orders.size();
            }
        });

        int totalInserted = results.length;
        log.info("批量插入订单成功: 数量={}", totalInserted);
        return totalInserted;
    }

    /**
     * 更新订单状态（实时更新）
     * 
     * Doris核心优势：支持实时UPDATE操作
     * ClickHouse主要支持追加，更新需要重建表或使用FINAL（性能差）
     */
    @Transactional
    public int updateOrderStatus(Long orderId, String newStatus) {
        String sql = """
            UPDATE orders 
            SET status = ?, update_time = NOW()
            WHERE order_id = ?
            """;
        
        try {
            int count = jdbcTemplate.update(sql, newStatus, orderId);
            log.info("更新订单状态成功: orderId={}, newStatus={}, count={}", 
                    orderId, newStatus, count);
            return count;
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}", orderId, e);
            throw new RuntimeException("更新订单状态失败", e);
        }
    }

    /**
     * 删除订单（实时删除）
     */
    public int deleteOrder(Long orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        
        try {
            int count = jdbcTemplate.update(sql, orderId);
            log.info("删除订单成功: orderId={}, count={}", orderId, count);
            return count;
        } catch (Exception e) {
            log.error("删除订单失败: orderId={}", orderId, e);
            throw new RuntimeException("删除订单失败", e);
        }
    }

    // ========== 多表JOIN查询（Doris核心优势）==========

    /**
     * 用户订单关联查询（多表JOIN）
     * 
     * Doris优势：
     * - CBO（Cost-Based Optimizer）优化
     * - 复杂JOIN性能优于ClickHouse
     * - 支持多种JOIN类型
     */
    public List<Map<String, Object>> getUserOrders(Long userId) {
        String sql = """
            SELECT 
                u.user_id,
                u.username,
                u.city,
                o.order_id,
                o.product_name,
                o.amount,
                o.status,
                o.order_time
            FROM users u
            LEFT JOIN orders o ON u.user_id = o.user_id
            WHERE u.user_id = ?
            ORDER BY o.order_time DESC
            """;

        return jdbcTemplate.queryForList(sql, userId);
    }

    /**
     * 按城市统计订单（多表JOIN + 聚合）
     * 
     * 展示Doris在复杂JOIN + 聚合场景下的优势
     */
    public List<Map<String, Object>> getOrderStatsByCity(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                u.city,
                COUNT(DISTINCT u.user_id) AS user_count,
                COUNT(o.order_id) AS order_count,
                SUM(o.amount) AS total_amount,
                AVG(o.amount) AS avg_amount
            FROM users u
            LEFT JOIN orders o ON u.user_id = o.user_id
            WHERE o.order_time >= ? AND o.order_time <= ?
            GROUP BY u.city
            ORDER BY total_amount DESC
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 用户订单汇总（多表JOIN + 复杂聚合）
     */
    public List<Map<String, Object>> getUserOrderSummary(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                u.user_id,
                u.username,
                u.city,
                COUNT(o.order_id) AS order_count,
                SUM(o.amount) AS total_amount,
                AVG(o.amount) AS avg_amount,
                MAX(o.order_time) AS last_order_time
            FROM users u
            LEFT JOIN orders o ON u.user_id = o.user_id
            WHERE o.order_time >= ? AND o.order_time <= ?
            GROUP BY u.user_id, u.username, u.city
            HAVING order_count > 0
            ORDER BY total_amount DESC
            LIMIT 100
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 三表关联查询（展示复杂JOIN能力）
     * 
     * 假设有产品表，演示三表JOIN
     */
    public List<Map<String, Object>> getComplexJoinQuery() {
        // 这里演示多表JOIN的能力
        // 实际场景可能包括：用户 -> 订单 -> 订单详情 -> 产品
        String sql = """
            SELECT 
                u.user_id,
                u.username,
                u.city,
                COUNT(DISTINCT o.order_id) AS order_count,
                SUM(o.amount) AS total_amount
            FROM users u
            INNER JOIN orders o ON u.user_id = o.user_id
            WHERE o.status IN ('paid', 'shipped', 'completed')
            GROUP BY u.user_id, u.username, u.city
            ORDER BY total_amount DESC
            LIMIT 50
            """;

        return jdbcTemplate.queryForList(sql);
    }

    // ========== 物化视图查询 ==========

    /**
     * 从物化视图查询（自动查询重写）
     * 
     * Doris物化视图优势：
     * - 支持多表物化视图
     * - 自动查询重写（无需修改SQL）
     * - 显著提高查询性能
     */
    public List<Map<String, Object>> queryFromMaterializedView(
            LocalDateTime startTime, LocalDateTime endTime) {
        // 注意：这里写的SQL和物化视图定义可能不完全匹配
        // 但Doris会自动重写查询，使用物化视图
        String sql = """
            SELECT 
                user_id,
                username,
                city,
                order_date,
                order_count,
                total_amount,
                avg_amount
            FROM user_order_mv
            WHERE order_date >= DATE(?) AND order_date <= DATE(?)
            ORDER BY total_amount DESC
            LIMIT 100
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    // ========== Aggregate模型查询 ==========

    /**
     * 查询订单汇总（Aggregate模型自动聚合）
     */
    public List<Map<String, Object>> getOrderSummary(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                user_id,
                order_date,
                total_orders,
                total_amount,
                avg_amount
            FROM order_summary
            WHERE order_date >= DATE(?) AND order_date <= DATE(?)
            ORDER BY total_amount DESC
            LIMIT 100
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    // ========== 统计查询 ==========

    /**
     * 获取用户统计
     */
    public Map<String, Object> getUserStats() {
        String sql = """
            SELECT 
                COUNT(*) AS total_users,
                COUNT(DISTINCT city) AS city_count,
                COUNT(DISTINCT country) AS country_count,
                AVG(age) AS avg_age
            FROM users
            """;

        try {
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Map.of();
        }
    }

    /**
     * 获取订单统计
     */
    public Map<String, Object> getOrderStats(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                COUNT(*) AS total_orders,
                COUNT(DISTINCT user_id) AS unique_users,
                SUM(amount) AS total_amount,
                AVG(amount) AS avg_amount,
                COUNT(CASE WHEN status = 'completed' THEN 1 END) AS completed_orders
            FROM orders
            WHERE order_time >= ? AND order_time <= ?
            """;

        try {
            return jdbcTemplate.queryForMap(sql,
                    startTime.format(DATE_TIME_FORMATTER),
                    endTime.format(DATE_TIME_FORMATTER));
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            return Map.of();
        }
    }

    /**
     * 按状态统计订单
     */
    public List<Map<String, Object>> getOrderStatsByStatus(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                status,
                COUNT(*) AS order_count,
                SUM(amount) AS total_amount,
                AVG(amount) AS avg_amount
            FROM orders
            WHERE order_time >= ? AND order_time <= ?
            GROUP BY status
            ORDER BY order_count DESC
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }
}
