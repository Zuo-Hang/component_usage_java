package com.example.nacos.controller;

import com.example.nacos.service.NacosConfigService;
import com.example.nacos.service.NacosDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos操作控制器
 * 提供服务发现和配置管理的REST API
 */
@Slf4j
@RestController
@RequestMapping("/nacos")
public class NacosController {

    @Autowired
    private NacosDiscoveryService discoveryService;

    @Autowired
    private NacosConfigService configService;

    // ========== 服务发现相关接口 ==========

    /**
     * 获取所有已注册的服务
     * GET /nacos/services
     */
    @GetMapping("/services")
    public Map<String, Object> getAllServices() {
        List<String> services = discoveryService.getAllServices();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("services", services);
        response.put("count", services.size());
        return response;
    }

    /**
     * 获取指定服务的实例列表
     * GET /nacos/services/{serviceName}/instances
     */
    @GetMapping("/services/{serviceName}/instances")
    public Map<String, Object> getServiceInstances(@PathVariable String serviceName) {
        List<ServiceInstance> instances = discoveryService.getServiceInstances(serviceName);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName);
        response.put("instances", instances);
        response.put("count", instances.size());
        return response;
    }

    /**
     * 获取服务的第一个可用实例
     * GET /nacos/services/{serviceName}/instance
     */
    @GetMapping("/services/{serviceName}/instance")
    public Map<String, Object> getFirstInstance(@PathVariable String serviceName) {
        ServiceInstance instance = discoveryService.getFirstInstance(serviceName);
        Map<String, Object> response = new HashMap<>();
        if (instance != null) {
            response.put("success", true);
            response.put("serviceName", serviceName);
            response.put("instance", instance);
            response.put("url", String.format("http://%s:%d", 
                    instance.getHost(), instance.getPort()));
        } else {
            response.put("success", false);
            response.put("message", "服务 " + serviceName + " 不可用");
        }
        return response;
    }

    /**
     * 构建服务调用URL
     * GET /nacos/services/{serviceName}/url?path=/api/test
     */
    @GetMapping("/services/{serviceName}/url")
    public Map<String, Object> buildServiceUrl(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "/") String path) {
        try {
            String url = discoveryService.buildServiceUrl(serviceName, path);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("serviceName", serviceName);
            response.put("path", path);
            response.put("url", url);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    // ========== 配置管理相关接口 ==========

    /**
     * 获取应用配置信息
     * GET /nacos/config/app
     */
    @GetMapping("/config/app")
    public Map<String, Object> getAppConfig() {
        NacosConfigService.AppConfig config = configService.getAppConfig();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("config", config);
        return response;
    }

    /**
     * 获取配置
     * GET /nacos/config?dataId=test.properties&group=DEFAULT_GROUP
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig(
            @RequestParam String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group) {
        String content = configService.getConfig(dataId, group);
        Map<String, Object> response = new HashMap<>();
        if (content != null) {
            response.put("success", true);
            response.put("dataId", dataId);
            response.put("group", group);
            response.put("content", content);
        } else {
            response.put("success", false);
            response.put("message", "配置不存在或获取失败");
        }
        return response;
    }

    /**
     * 发布配置
     * POST /nacos/config
     * Body: {"dataId": "test.properties", "group": "DEFAULT_GROUP", "content": "key=value"}
     */
    @PostMapping("/config")
    public Map<String, Object> publishConfig(@RequestBody Map<String, String> request) {
        String dataId = request.get("dataId");
        String group = request.getOrDefault("group", "DEFAULT_GROUP");
        String content = request.get("content");
        
        boolean result = configService.publishConfig(dataId, group, content);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("dataId", dataId);
        response.put("group", group);
        response.put("message", result ? "配置发布成功" : "配置发布失败");
        return response;
    }

    /**
     * 删除配置
     * DELETE /nacos/config?dataId=test.properties&group=DEFAULT_GROUP
     */
    @DeleteMapping("/config")
    public Map<String, Object> removeConfig(
            @RequestParam String dataId,
            @RequestParam(defaultValue = "DEFAULT_GROUP") String group) {
        boolean result = configService.removeConfig(dataId, group);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("dataId", dataId);
        response.put("group", group);
        response.put("message", result ? "配置删除成功" : "配置删除失败");
        return response;
    }

    /**
     * 健康检查
     * GET /nacos/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "nacos-example");
        return response;
    }
}
