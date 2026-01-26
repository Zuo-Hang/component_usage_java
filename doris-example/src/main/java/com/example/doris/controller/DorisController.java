package com.example.doris.controller;

import com.example.doris.model.Order;
import com.example.doris.model.User;
import com.example.doris.service.DorisDataService;
import com.example.doris.service.DorisTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Doris操作控制器
 * 
 * 重点展示Doris的核心优势：
 * 1. MySQL协议兼容
 * 2. 实时更新和删除
 * 3. 多表JOIN查询（CBO优化）
 * 4. 物化视图查询重写
 */
@Slf4j
@RestController
@RequestMapping("/doris")
public class DorisController {

    @Autowired
    private DorisTableService tableService;

    @Autowired
    private DorisDataService dataService;

    // ========== 表管理接口 ==========

    /**
     * 创建用户表（Duplicate模型）
     * POST /doris/tables/users
     */
    @PostMapping("/tables/users")
    public Map<String, Object> createUserTable() {
        try {
            tableService.createUserTable();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户表创建成功（Duplicate模型）");
            return response;
        } catch (Exception e) {
            log.error("创建用户表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建用户表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 创建订单表（Unique模型，支持实时更新）
     * POST /doris/tables/orders
     */
    @PostMapping("/tables/orders")
    public Map<String, Object> createOrderTable() {
        try {
            tableService.createOrderTable();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单表创建成功（Unique模型，支持实时更新）");
            return response;
        } catch (Exception e) {
            log.error("创建订单表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建订单表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 创建订单汇总表（Aggregate模型）
     * POST /doris/tables/order-summary
     */
    @PostMapping("/tables/order-summary")
    public Map<String, Object> createOrderSummaryTable() {
        try {
            tableService.createOrderSummaryTable();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单汇总表创建成功（Aggregate模型，自动预聚合）");
            return response;
        } catch (Exception e) {
            log.error("创建订单汇总表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建订单汇总表失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 创建物化视图（多表物化视图）
     * POST /doris/tables/materialized-view
     */
    @PostMapping("/tables/materialized-view")
    public Map<String, Object> createMaterializedView() {
        try {
            tableService.createMaterializedView();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "物化视图创建成功（多表物化视图，支持查询重写）");
            return response;
        } catch (Exception e) {
            log.error("创建物化视图失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建物化视图失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 数据导入接口 ==========

    /**
     * 批量插入用户
     * POST /doris/data/users/batch
     */
    @PostMapping("/data/users/batch")
    public Map<String, Object> batchInsertUsers(@RequestBody List<User> users) {
        try {
            int count = dataService.batchInsertUsers(users);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertedCount", count);
            response.put("message", "批量插入用户成功");
            return response;
        } catch (Exception e) {
            log.error("批量插入用户失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量插入用户失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 批量插入订单
     * POST /doris/data/orders/batch
     */
    @PostMapping("/data/orders/batch")
    public Map<String, Object> batchInsertOrders(@RequestBody List<Order> orders) {
        try {
            int count = dataService.batchInsertOrders(orders);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insertedCount", count);
            response.put("message", "批量插入订单成功");
            return response;
        } catch (Exception e) {
            log.error("批量插入订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量插入订单失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 实时更新接口（Doris核心优势）==========

    /**
     * 更新用户信息（实时更新）
     * PUT /doris/data/users/{userId}?email=new@example.com&city=Shanghai
     */
    @PutMapping("/data/users/{userId}")
    public Map<String, Object> updateUser(
            @PathVariable Long userId,
            @RequestParam String email,
            @RequestParam String city) {
        try {
            int count = dataService.updateUser(userId, email, city);
            Map<String, Object> response = new HashMap<>();
            response.put("success", count > 0);
            response.put("updatedCount", count);
            response.put("message", count > 0 ? "更新用户成功" : "用户不存在");
            response.put("note", "Doris支持实时UPDATE操作（ClickHouse主要支持追加）");
            return response;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新用户失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除用户（实时删除）
     * DELETE /doris/data/users/{userId}
     */
    @DeleteMapping("/data/users/{userId}")
    public Map<String, Object> deleteUser(@PathVariable Long userId) {
        try {
            int count = dataService.deleteUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", count > 0);
            response.put("deletedCount", count);
            response.put("message", count > 0 ? "删除用户成功" : "用户不存在");
            response.put("note", "Doris支持实时DELETE操作（ClickHouse主要支持追加）");
            return response;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 更新订单状态（实时更新）
     * PUT /doris/data/orders/{orderId}/status?status=shipped
     */
    @PutMapping("/data/orders/{orderId}/status")
    public Map<String, Object> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            int count = dataService.updateOrderStatus(orderId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", count > 0);
            response.put("updatedCount", count);
            response.put("message", count > 0 ? "更新订单状态成功" : "订单不存在");
            response.put("note", "Doris支持实时UPDATE操作，适合订单状态更新场景");
            return response;
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新订单状态失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除订单（实时删除）
     * DELETE /doris/data/orders/{orderId}
     */
    @DeleteMapping("/data/orders/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable Long orderId) {
        try {
            int count = dataService.deleteOrder(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", count > 0);
            response.put("deletedCount", count);
            response.put("message", count > 0 ? "删除订单成功" : "订单不存在");
            return response;
        } catch (Exception e) {
            log.error("删除订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除订单失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 多表JOIN查询接口（Doris核心优势）==========

    /**
     * 查询用户订单（多表JOIN）
     * GET /doris/query/user-orders/{userId}
     */
    @GetMapping("/query/user-orders/{userId}")
    public Map<String, Object> getUserOrders(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> orders = dataService.getUserOrders(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("orders", orders);
            response.put("count", orders.size());
            response.put("note", "Doris的CBO优化器使多表JOIN性能优于ClickHouse");
            return response;
        } catch (Exception e) {
            log.error("查询用户订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 按城市统计订单（多表JOIN + 聚合）
     * GET /doris/query/stats/city?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/stats/city")
    public Map<String, Object> getOrderStatsByCity(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> stats = dataService.getOrderStatsByCity(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("note", "Doris在复杂JOIN + 聚合场景下性能优异");
            return response;
        } catch (Exception e) {
            log.error("按城市统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 用户订单汇总（多表JOIN + 复杂聚合）
     * GET /doris/query/user-summary?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/query/user-summary")
    public Map<String, Object> getUserOrderSummary(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> summary = dataService.getUserOrderSummary(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            response.put("count", summary.size());
            return response;
        } catch (Exception e) {
            log.error("用户订单汇总失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "汇总失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 复杂JOIN查询
     * GET /doris/query/complex-join
     */
    @GetMapping("/query/complex-join")
    public Map<String, Object> getComplexJoinQuery() {
        try {
            List<Map<String, Object>> results = dataService.getComplexJoinQuery();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("note", "Doris的CBO优化器使复杂JOIN查询性能优异");
            return response;
        } catch (Exception e) {
            log.error("复杂JOIN查询失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 物化视图查询 ==========

    /**
     * 从物化视图查询（自动查询重写）
     * GET /doris/query/materialized-view?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
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
            response.put("note", "Doris支持多表物化视图和自动查询重写");
            return response;
        } catch (Exception e) {
            log.error("从物化视图查询失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return response;
        }
    }

    // ========== 统计接口 ==========

    /**
     * 获取用户统计
     * GET /doris/stats/users
     */
    @GetMapping("/stats/users")
    public Map<String, Object> getUserStats() {
        try {
            Map<String, Object> stats = dataService.getUserStats();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 获取订单统计
     * GET /doris/stats/orders?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/stats/orders")
    public Map<String, Object> getOrderStats(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            Map<String, Object> stats = dataService.getOrderStats(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 按状态统计订单
     * GET /doris/stats/orders-by-status?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
     */
    @GetMapping("/stats/orders-by-status")
    public Map<String, Object> getOrderStatsByStatus(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            List<Map<String, Object>> stats = dataService.getOrderStatsByStatus(start, end);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return response;
        } catch (Exception e) {
            log.error("按状态统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "统计失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 健康检查
     * GET /doris/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "doris-example");
        return response;
    }
}
