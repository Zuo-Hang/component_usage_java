package com.example.etcd.controller;

import com.example.etcd.service.EtcdConfigService;
import com.example.etcd.service.EtcdKeyValueService;
import com.example.etcd.service.EtcdLeaseService;
import com.example.etcd.service.EtcdServiceDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Etcd操作控制器
 * 
 * 提供REST API接口：
 * 1. 键值存储操作
 * 2. 服务发现操作
 * 3. 配置管理操作
 * 4. 租约操作
 */
@Slf4j
@RestController
@RequestMapping("/etcd")
public class EtcdController {

    @Autowired
    private EtcdKeyValueService keyValueService;

    @Autowired
    private EtcdServiceDiscoveryService serviceDiscoveryService;

    @Autowired
    private EtcdConfigService configService;

    @Autowired
    private EtcdLeaseService leaseService;

    // ========== 键值存储接口 ==========

    /**
     * 写入键值对
     * PUT /etcd/kv/{key}?value=xxx
     */
    @PutMapping("/kv/{key}")
    public Map<String, Object> putKeyValue(
            @PathVariable String key,
            @RequestParam String value) {
        boolean success = keyValueService.put(key, value);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("key", key);
        response.put("value", value);
        return response;
    }

    /**
     * 读取键值对
     * GET /etcd/kv/{key}
     */
    @GetMapping("/kv/{key}")
    public Map<String, Object> getKeyValue(@PathVariable String key) {
        String value = keyValueService.get(key);
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("exists", value != null);
        return response;
    }

    /**
     * 删除键值对
     * DELETE /etcd/kv/{key}
     */
    @DeleteMapping("/kv/{key}")
    public Map<String, Object> deleteKeyValue(@PathVariable String key) {
        boolean success = keyValueService.delete(key);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("key", key);
        return response;
    }

    /**
     * 前缀查询
     * GET /etcd/kv/prefix/{prefix}
     */
    @GetMapping("/kv/prefix/{prefix}")
    public Map<String, Object> getByPrefix(@PathVariable String prefix) {
        Map<String, String> keyValues = keyValueService.getAllByPrefix(prefix);
        Map<String, Object> response = new HashMap<>();
        response.put("prefix", prefix);
        response.put("count", keyValues.size());
        response.put("keyValues", keyValues);
        return response;
    }

    /**
     * 批量写入键值对
     * POST /etcd/kv/batch
     */
    @PostMapping("/kv/batch")
    public Map<String, Object> batchPut(@RequestBody Map<String, String> keyValues) {
        int successCount = keyValueService.batchPut(keyValues);
        Map<String, Object> response = new HashMap<>();
        response.put("total", keyValues.size());
        response.put("successCount", successCount);
        return response;
    }

    // ========== 服务发现接口 ==========

    /**
     * 服务注册
     * POST /etcd/service/register?serviceName=xxx&instanceId=xxx&instanceInfo=xxx&ttl=60
     */
    @PostMapping("/service/register")
    public Map<String, Object> registerService(
            @RequestParam String serviceName,
            @RequestParam String instanceId,
            @RequestParam String instanceInfo,
            @RequestParam(defaultValue = "60") long ttl) {
        long leaseId = serviceDiscoveryService.registerService(
                serviceName, instanceId, instanceInfo, ttl);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName);
        response.put("instanceId", instanceId);
        response.put("leaseId", leaseId);
        response.put("message", "服务注册成功，租约自动续约");
        return response;
    }

    /**
     * 服务注销
     * DELETE /etcd/service/unregister?serviceName=xxx&instanceId=xxx
     */
    @DeleteMapping("/service/unregister")
    public Map<String, Object> unregisterService(
            @RequestParam String serviceName,
            @RequestParam String instanceId) {
        boolean success = serviceDiscoveryService.unregisterService(serviceName, instanceId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("serviceName", serviceName);
        response.put("instanceId", instanceId);
        return response;
    }

    /**
     * 发现服务实例
     * GET /etcd/service/discover/{serviceName}
     */
    @GetMapping("/service/discover/{serviceName}")
    public Map<String, Object> discoverService(@PathVariable String serviceName) {
        List<String> instances = serviceDiscoveryService.discoverService(serviceName);
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("instances", instances);
        response.put("count", instances.size());
        return response;
    }

    /**
     * 获取所有服务
     * GET /etcd/service/list
     */
    @GetMapping("/service/list")
    public Map<String, Object> getAllServices() {
        List<String> services = serviceDiscoveryService.getAllServices();
        Map<String, Object> response = new HashMap<>();
        response.put("services", services);
        response.put("count", services.size());
        return response;
    }

    /**
     * 获取服务的第一个可用实例
     * GET /etcd/service/first/{serviceName}
     */
    @GetMapping("/service/first/{serviceName}")
    public Map<String, Object> getFirstInstance(@PathVariable String serviceName) {
        String instance = serviceDiscoveryService.getFirstInstance(serviceName);
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("instance", instance);
        response.put("exists", instance != null);
        return response;
    }

    // ========== 配置管理接口 ==========

    /**
     * 写入配置
     * PUT /etcd/config/{application}/{key}?value=xxx
     */
    @PutMapping("/config/{application}/{key}")
    public Map<String, Object> putConfig(
            @PathVariable String application,
            @PathVariable String key,
            @RequestParam String value) {
        boolean success = configService.putConfig(application, key, value);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("application", application);
        response.put("key", key);
        response.put("value", value);
        return response;
    }

    /**
     * 读取配置
     * GET /etcd/config/{application}/{key}
     */
    @GetMapping("/config/{application}/{key}")
    public Map<String, Object> getConfig(
            @PathVariable String application,
            @PathVariable String key) {
        String value = configService.getConfig(application, key);
        Map<String, Object> response = new HashMap<>();
        response.put("application", application);
        response.put("key", key);
        response.put("value", value);
        response.put("exists", value != null);
        return response;
    }

    /**
     * 删除配置
     * DELETE /etcd/config/{application}/{key}
     */
    @DeleteMapping("/config/{application}/{key}")
    public Map<String, Object> deleteConfig(
            @PathVariable String application,
            @PathVariable String key) {
        boolean success = configService.deleteConfig(application, key);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("application", application);
        response.put("key", key);
        return response;
    }

    /**
     * 获取应用的所有配置
     * GET /etcd/config/{application}
     */
    @GetMapping("/config/{application}")
    public Map<String, Object> getAllConfigs(@PathVariable String application) {
        Map<String, String> configs = configService.getAllConfigs(application);
        Map<String, Object> response = new HashMap<>();
        response.put("application", application);
        response.put("configs", configs);
        response.put("count", configs.size());
        return response;
    }

    /**
     * 批量写入配置
     * POST /etcd/config/{application}/batch
     */
    @PostMapping("/config/{application}/batch")
    public Map<String, Object> batchPutConfigs(
            @PathVariable String application,
            @RequestBody Map<String, String> configs) {
        int successCount = configService.batchPutConfigs(application, configs);
        Map<String, Object> response = new HashMap<>();
        response.put("application", application);
        response.put("total", configs.size());
        response.put("successCount", successCount);
        return response;
    }

    // ========== 租约接口 ==========

    /**
     * 创建租约
     * POST /etcd/lease/grant?ttl=60
     */
    @PostMapping("/lease/grant")
    public Map<String, Object> grantLease(@RequestParam long ttl) {
        long leaseId = leaseService.grantLease(ttl);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("leaseId", leaseId);
        response.put("ttl", ttl);
        return response;
    }

    /**
     * 续约
     * POST /etcd/lease/keepalive/{leaseId}
     */
    @PostMapping("/lease/keepalive/{leaseId}")
    public Map<String, Object> keepAlive(@PathVariable long leaseId) {
        long ttl = leaseService.keepAlive(leaseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ttl > 0);
        response.put("leaseId", leaseId);
        response.put("ttl", ttl);
        return response;
    }

    /**
     * 撤销租约
     * DELETE /etcd/lease/{leaseId}
     */
    @DeleteMapping("/lease/{leaseId}")
    public Map<String, Object> revokeLease(@PathVariable long leaseId) {
        boolean success = leaseService.revokeLease(leaseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("leaseId", leaseId);
        return response;
    }

    /**
     * 查询租约信息
     * GET /etcd/lease/{leaseId}
     */
    @GetMapping("/lease/{leaseId}")
    public Map<String, Object> getLeaseInfo(@PathVariable long leaseId) {
        long ttl = leaseService.getLeaseTTL(leaseId);
        Map<String, Object> response = new HashMap<>();
        response.put("leaseId", leaseId);
        response.put("ttl", ttl);
        response.put("exists", ttl >= 0);
        return response;
    }

    /**
     * 健康检查
     * GET /etcd/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "etcd-example");
        return response;
    }
}
