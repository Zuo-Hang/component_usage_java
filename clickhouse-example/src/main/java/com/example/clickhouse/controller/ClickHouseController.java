package com.example.clickhouse.controller;

import com.example.clickhouse.model.UserBehaviorLog;
import com.example.clickhouse.service.ClickHouseDataService;
import com.example.clickhouse.service.ClickHouseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse操作控制器
 * 
 * 提供生产环境典型场景的REST API：
 * - 批量数据导入
 * - 聚合分析查询
 * - 时间序列分析
 * - 物化视图查询
 */
@Slf4j
@RestController
@RequestMapping("/clickhouse")
public class ClickHouseController {

    @Autowired
    private ClickHouseTableService tableService;

    @Autowired
    private ClickHouseDataService dataService;

    // ========== 表管理接口 ==========

    /**
     * 创建用户行为日志表
     * POST /clickhouse/tables/user-behavior-log
     */
    @PostMapping("/tables/user-behavior-log")
    public Map<String, Object> createUserBehaviorLogTable() {
        try {
            tableService.createUserBehaviorLogTable();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户行为日志表创建成功");
            return response;
        } catch (Exception e) {
            log.error("创建表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 创建物化视图
     * POST /clickhouse/tables/materialized-view
     */
    @PostMapping("/tables/materialized-view")
    public Map<String, Object> createMaterializedView() {
        try {
            tableService.createMaterializedView();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "物化视图创建成功");
            return response;
        } catch (Exception e) {
            log.error("创建物化视图失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建物化视图失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取表信息
     * GET /clickhouse/tables/{tableName}/info
     */
    @GetMapping("/tables/{tableName}/info")
    public Map<String, Object> getTableInfo(@PathVariable String tableName) {
        try {
            tableService.showTableInfo(tableName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "表信息已输出到日志");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取表信息失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 数据导入接口 ==========

    /**
     * 批量插入用户行为日志
     * POST /clickhouse/data/batch-insert
     */
    @PostMapping("/data/batch-insert")
    public Map<String, Object> batchInsert(@RequestBody List<UserBehaviorLog> logs) {
        try {
            int count = dataService.batchInsertUserBehaviorLogs(logs);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertedCount", count);
            response.put("message", "批量插入成功");
            return response;
        } catch (Exception e) {
            log.error("批量插入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量插入失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 批量插入（VALUES格式，性能最优）
     * POST /clickhouse/data/batch-insert-values
     */
    @PostMapping("/data/batch-insert-values")
    public Map<String, Object> batchInsertWithValues(@RequestBody List<UserBehaviorLog> logs) {
        try {
            int count = dataService.batchInsertWithValues(logs);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertedCount", count);
            response.put("message", "VALUES格式批量插入成功");
            return response;
        } catch (Exception e) {
            log.error("批量插入失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量插入失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 查询接口 ==========

    /**
     * 查询用户行为日志
     * GET /clickhouse/query/logs?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00&limit=100
     */
    @GetMapping("/query/logs")
    public Map<String, Object> queryLogs(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "100") Integer limit) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> logs = dataService.queryUserBehaviorLogs(start, end, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("count", logs.size());
            return response;
        } catch (Exception e) {
            log.error("查询日志失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 按事件类型统计
     * GET /clickhouse/query/stats/event-type?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/stats/event-type")
    public Map<String, Object> countByEventType(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> stats = dataService.countByEventType(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 按小时统计
     * GET /clickhouse/query/stats/hourly?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/stats/hourly")
    public Map<String, Object> countByHour(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> stats = dataService.countByHour(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("按小时统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 按设备类型统计
     * GET /clickhouse/query/stats/device?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/stats/device")
    public Map<String, Object> countByDeviceType(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> stats = dataService.countByDeviceType(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("按设备类型统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取热门页面
     * GET /clickhouse/query/top-pages?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00&topN=10
     */
    @GetMapping("/query/top-pages")
    public Map<String, Object> getTopPages(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "10") Integer topN) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> pages = dataService.getTopPages(start, end, topN);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pages", pages);
            return response;
        } catch (Exception e) {
            log.error("获取热门页面失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 分析用户行为路径
     * GET /clickhouse/query/user-path?userId=user123&startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/user-path")
    public Map<String, Object> analyzeUserPath(
            @RequestParam String userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> path = dataService.analyzeUserPath(userId, start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("path", path);
            response.put("stepCount", path.size());
            return response;
        } catch (Exception e) {
            log.error("分析用户路径失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "分析失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 从物化视图查询（预聚合数据）
     * GET /clickhouse/query/materialized-view?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/materialized-view")
    public Map<String, Object> queryFromMaterializedView(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> data = dataService.queryFromMaterializedView(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("message", "从物化视图查询成功（性能优化）");
            return response;
        } catch (Exception e) {
            log.error("从物化视图查询失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 监控和管理接口 ==========

    /**
     * 获取表统计信息
     * GET /clickhouse/monitor/table-stats
     */
    @GetMapping("/monitor/table-stats")
    public Map<String, Object> getTableStats() {
        try {
            Map<String, Object> stats = dataService.getTableStats();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("获取表统计信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取分区信息
     * GET /clickhouse/monitor/partitions
     */
    @GetMapping("/monitor/partitions")
    public Map<String, Object> getPartitionInfo() {
        try {
            List<Map<String, Object>> partitions = dataService.getPartitionInfo();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("partitions", partitions);
            response.put("count", partitions.size());
            return response;
        } catch (Exception e) {
            log.error("获取分区信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分区信息失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 健康检查
     * GET /clickhouse/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "clickhouse-example");
        return response;
    }
}
