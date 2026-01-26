package com.example.clickhouse.service;

import com.example.clickhouse.model.UserBehaviorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse数据操作服务
 * 
 * 生产环境最佳实践：
 * 1. 批量插入（提高性能）
 * 2. 异步插入（可选，降低延迟）
 * 3. 分区管理（按时间分区）
 * 4. 数据压缩优化
 */
@Slf4j
@Service
public class ClickHouseDataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 批量插入用户行为日志
     * 
     * 生产环境要点：
     * - 批量插入性能远高于单条插入
     * - 建议批量大小：1000-10000条
     * - 使用VALUES格式，性能最好
     */
    public int batchInsertUserBehaviorLogs(List<UserBehaviorLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return 0;
        }

        String sql = """
            INSERT INTO user_behavior_log 
            (event_time, user_id, event_type, page_url, duration, device_type, ip_address)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserBehaviorLog log = logs.get(i);
                ps.setString(1, log.getEventTime().format(DATE_TIME_FORMATTER));
                ps.setString(2, log.getUserId());
                ps.setString(3, log.getEventType());
                ps.setString(4, log.getPageUrl());
                ps.setInt(5, log.getDuration() != null ? log.getDuration() : 0);
                ps.setString(6, log.getDeviceType());
                ps.setString(7, log.getIpAddress());
            }

            @Override
            public int getBatchSize() {
                return logs.size();
            }
        });

        int totalInserted = results.length;
        log.info("批量插入用户行为日志成功: 数量={}", totalInserted);
        return totalInserted;
    }

    /**
     * 使用VALUES格式批量插入（性能最优）
     * 
     * 生产环境推荐方式：
     * - 直接构建VALUES SQL，性能最好
     * - 适合大批量数据导入（万级、十万级）
     */
    public int batchInsertWithValues(List<UserBehaviorLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder(
                "INSERT INTO user_behavior_log " +
                "(event_time, user_id, event_type, page_url, duration, device_type, ip_address) VALUES ");

        for (int i = 0; i < logs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            UserBehaviorLog log = logs.get(i);
            sql.append(String.format(
                    "('%s', '%s', '%s', '%s', %d, '%s', '%s')",
                    log.getEventTime().format(DATE_TIME_FORMATTER),
                    log.getUserId(),
                    log.getEventType(),
                    log.getPageUrl(),
                    log.getDuration() != null ? log.getDuration() : 0,
                    log.getDeviceType(),
                    log.getIpAddress()
            ));
        }

        try {
            int count = jdbcTemplate.update(sql.toString());
            log.info("VALUES格式批量插入成功: 数量={}", count);
            return count;
        } catch (Exception e) {
            log.error("批量插入失败", e);
            throw new RuntimeException("批量插入失败", e);
        }
    }

    /**
     * 查询用户行为日志（基础查询）
     */
    public List<Map<String, Object>> queryUserBehaviorLogs(
            LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        String sql = """
            SELECT 
                event_time,
                user_id,
                event_type,
                page_url,
                duration,
                device_type,
                ip_address
            FROM user_behavior_log
            WHERE event_time >= ? AND event_time <= ?
            ORDER BY event_time DESC
            LIMIT ?
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER),
                limit);
    }

    /**
     * 按事件类型统计（聚合查询）
     * 
     * 生产环境典型场景：
     * - 统计各事件类型的数量
     * - 分析用户行为分布
     */
    public List<Map<String, Object>> countByEventType(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                event_type,
                count() AS event_count,
                uniqExact(user_id) AS unique_users,
                avg(duration) AS avg_duration,
                sum(duration) AS total_duration
            FROM user_behavior_log
            WHERE event_time >= ? AND event_time <= ?
            GROUP BY event_type
            ORDER BY event_count DESC
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 按时间维度统计（小时级别）
     * 
     * 生产环境典型场景：
     * - 分析用户行为的时间分布
     * - 识别高峰时段
     */
    public List<Map<String, Object>> countByHour(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                toStartOfHour(event_time) AS hour_time,
                count() AS event_count,
                uniqExact(user_id) AS unique_users,
                avg(duration) AS avg_duration
            FROM user_behavior_log
            WHERE event_time >= ? AND event_time <= ?
            GROUP BY hour_time
            ORDER BY hour_time
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 按设备类型统计
     */
    public List<Map<String, Object>> countByDeviceType(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                device_type,
                count() AS event_count,
                uniqExact(user_id) AS unique_users,
                avg(duration) AS avg_duration
            FROM user_behavior_log
            WHERE event_time >= ? AND event_time <= ?
            GROUP BY device_type
            ORDER BY event_count DESC
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 查询热门页面（TOP N）
     * 
     * 生产环境典型场景：
     * - 分析最受欢迎的页面
     * - 优化页面性能
     */
    public List<Map<String, Object>> getTopPages(
            LocalDateTime startTime, LocalDateTime endTime, int topN) {
        String sql = """
            SELECT 
                page_url,
                count() AS view_count,
                uniqExact(user_id) AS unique_visitors,
                avg(duration) AS avg_duration
            FROM user_behavior_log
            WHERE event_time >= ? AND event_time <= ?
            AND event_type = 'page_view'
            GROUP BY page_url
            ORDER BY view_count DESC
            LIMIT ?
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER),
                topN);
    }

    /**
     * 用户行为路径分析
     * 
     * 生产环境典型场景：
     * - 分析用户访问路径
     * - 优化页面跳转流程
     */
    public List<Map<String, Object>> analyzeUserPath(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                event_time,
                page_url,
                event_type,
                duration
            FROM user_behavior_log
            WHERE user_id = ?
            AND event_time >= ? AND event_time <= ?
            ORDER BY event_time ASC
            """;

        return jdbcTemplate.queryForList(sql,
                userId,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 使用物化视图查询（预聚合数据）
     * 
     * 生产环境优化策略：
     * - 物化视图已预聚合，查询速度更快
     * - 适合实时报表场景
     */
    public List<Map<String, Object>> queryFromMaterializedView(
            LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT 
                hour_time,
                event_type,
                device_type,
                sum(event_count) AS total_events,
                sum(total_duration) AS total_duration,
                sum(unique_users) AS total_users
            FROM user_behavior_hourly_mv
            WHERE hour_time >= ? AND hour_time <= ?
            GROUP BY hour_time, event_type, device_type
            ORDER BY hour_time DESC
            """;

        return jdbcTemplate.queryForList(sql,
                startTime.format(DATE_TIME_FORMATTER),
                endTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * 获取表统计信息
     */
    public Map<String, Object> getTableStats() {
        String sql = """
            SELECT 
                count() AS total_rows,
                uniqExact(user_id) AS unique_users,
                min(event_time) AS min_time,
                max(event_time) AS max_time
            FROM user_behavior_log
            """;

        try {
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            log.error("获取表统计信息失败", e);
            return Map.of();
        }
    }

    /**
     * 获取分区信息
     * 
     * 生产环境监控要点：
     * - 监控分区大小
     * - 检查分区数据分布
     * - 优化分区策略
     */
    public List<Map<String, Object>> getPartitionInfo() {
        String sql = """
            SELECT 
                partition,
                name,
                rows,
                bytes_on_disk,
                formatReadableSize(bytes_on_disk) AS disk_size,
                min_date,
                max_date
            FROM system.parts
            WHERE database = currentDatabase()
            AND table = 'user_behavior_log'
            AND active = 1
            ORDER BY partition DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }
}
