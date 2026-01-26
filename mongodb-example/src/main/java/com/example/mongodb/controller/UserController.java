package com.example.mongodb.controller;

import com.example.mongodb.model.User;
import com.example.mongodb.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户操作控制器
 * 提供MongoDB CRUD和查询的REST API
 */
@Slf4j
@RestController
@RequestMapping("/mongodb/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ========== 基本CRUD操作 ==========

    /**
     * 创建用户
     * POST /mongodb/users
     */
    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", created);
            response.put("message", "用户创建成功");
            return response;
        } catch (Exception e) {
            log.error("创建用户失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户创建失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 根据ID获取用户
     * GET /mongodb/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
        Optional<User> user = userService.findById(id);
        Map<String, Object> response = new HashMap<>();
        if (user.isPresent()) {
            response.put("success", true);
            response.put("user", user.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 更新用户
     * PUT /mongodb/users/{id}
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            User updated = userService.updateUser(id, user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", updated);
            response.put("message", "用户更新成功");
            return response;
        } catch (Exception e) {
            log.error("更新用户失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户更新失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 删除用户
     * DELETE /mongodb/users/{id}
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable String id) {
        boolean success = userService.deleteUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "用户删除成功" : "用户不存在");
        return response;
    }

    /**
     * 获取所有用户
     * GET /mongodb/users
     */
    @GetMapping
    public Map<String, Object> getAllUsers() {
        List<User> users = userService.getAllUsers();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return response;
    }

    // ========== 查询操作 ==========

    /**
     * 根据用户名查找
     * GET /mongodb/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        Map<String, Object> response = new HashMap<>();
        if (user.isPresent()) {
            response.put("success", true);
            response.put("user", user.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 根据年龄范围查找
     * GET /mongodb/users/age?min=18&max=30
     */
    @GetMapping("/age")
    public Map<String, Object> findByAgeRange(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<User> users = userService.findByAgeRange(min, max);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return response;
    }

    /**
     * 根据城市查找
     * GET /mongodb/users/city/{city}
     */
    @GetMapping("/city/{city}")
    public Map<String, Object> findByCity(@PathVariable String city) {
        List<User> users = userService.findByCity(city);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return response;
    }

    /**
     * 根据标签查找
     * GET /mongodb/users/tags?tags=java&tags=spring
     */
    @GetMapping("/tags")
    public Map<String, Object> findByTags(@RequestParam List<String> tags) {
        List<User> users = userService.findByTags(tags);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return response;
    }

    /**
     * 用户名模糊查询
     * GET /mongodb/users/search?username=john
     */
    @GetMapping("/search")
    public Map<String, Object> searchByUsername(@RequestParam String username) {
        List<User> users = userService.findByUsernameLike(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", users);
        response.put("count", users.size());
        return response;
    }

    /**
     * 分页查询
     * GET /mongodb/users/page?page=0&size=10&sortBy=username
     */
    @GetMapping("/page")
    public Map<String, Object> getUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy) {
        Page<User> userPage = userService.findUsersWithPagination(page, size, sortBy);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("users", userPage.getContent());
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);
        return response;
    }

    // ========== 聚合查询 ==========

    /**
     * 按城市统计用户数
     * GET /mongodb/users/stats/city
     */
    @GetMapping("/stats/city")
    public Map<String, Object> countUsersByCity() {
        List<Map<String, Object>> stats = userService.countUsersByCity();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);
        return response;
    }

    /**
     * 获取平均年龄
     * GET /mongodb/users/stats/avg-age
     */
    @GetMapping("/stats/avg-age")
    public Map<String, Object> getAverageAge() {
        double avgAge = userService.getAverageAge();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("averageAge", avgAge);
        return response;
    }

    /**
     * 按年龄分组统计
     * GET /mongodb/users/stats/age-distribution
     */
    @GetMapping("/stats/age-distribution")
    public Map<String, Object> getAgeDistribution() {
        List<Map<String, Object>> distribution = userService.groupByAgeRange();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("distribution", distribution);
        return response;
    }

    /**
     * 获取统计信息
     * GET /mongodb/users/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = userService.getStatistics();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return response;
    }

    // ========== 更新操作 ==========

    /**
     * 更新用户字段
     * PATCH /mongodb/users/{id}/field?field=age&value=25
     */
    @PatchMapping("/{id}/field")
    public Map<String, Object> updateUserField(
            @PathVariable String id,
            @RequestParam String field,
            @RequestParam Object value) {
        boolean success = userService.updateUserField(id, field, value);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "字段更新成功" : "字段更新失败");
        return response;
    }

    /**
     * 添加标签
     * POST /mongodb/users/{id}/tags?tag=java
     */
    @PostMapping("/{id}/tags")
    public Map<String, Object> addTag(
            @PathVariable String id,
            @RequestParam String tag) {
        boolean success = userService.addTag(id, tag);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "标签添加成功" : "标签添加失败");
        return response;
    }

    /**
     * 健康检查
     * GET /mongodb/users/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "mongodb-example");
        return response;
    }
}
