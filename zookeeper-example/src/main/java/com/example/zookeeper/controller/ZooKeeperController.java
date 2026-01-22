package com.example.zookeeper.controller;

import com.example.zookeeper.service.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZooKeeper示例控制器
 */
@Slf4j
@RestController
@RequestMapping("/zookeeper")
public class ZooKeeperController {

    @Autowired
    private ZooKeeperService zooKeeperService;

    /**
     * 创建持久化节点
     */
    @PostMapping("/create/persistent")
    public Map<String, Object> createPersistentNode(
            @RequestParam String path,
            @RequestParam(required = false, defaultValue = "") String data) {
        String createdPath = zooKeeperService.createPersistentNode(path, data);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", createdPath);
        result.put("message", "创建持久化节点成功");
        return result;
    }

    /**
     * 创建临时节点
     */
    @PostMapping("/create/ephemeral")
    public Map<String, Object> createEphemeralNode(
            @RequestParam String path,
            @RequestParam(required = false, defaultValue = "") String data) {
        String createdPath = zooKeeperService.createEphemeralNode(path, data);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", createdPath);
        result.put("message", "创建临时节点成功");
        return result;
    }

    /**
     * 获取节点数据
     */
    @GetMapping("/get")
    public Map<String, Object> getData(@RequestParam String path) {
        String data = zooKeeperService.getData(path);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", path);
        result.put("data", data);
        return result;
    }

    /**
     * 设置节点数据
     */
    @PutMapping("/set")
    public Map<String, Object> setData(
            @RequestParam String path,
            @RequestParam String data) {
        zooKeeperService.setData(path, data);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", path);
        result.put("message", "设置节点数据成功");
        return result;
    }

    /**
     * 检查节点是否存在
     */
    @GetMapping("/exists")
    public Map<String, Object> exists(@RequestParam String path) {
        boolean exists = zooKeeperService.exists(path);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", path);
        result.put("exists", exists);
        return result;
    }

    /**
     * 获取子节点列表
     */
    @GetMapping("/children")
    public Map<String, Object> getChildren(@RequestParam String path) {
        List<String> children = zooKeeperService.getChildren(path);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", path);
        result.put("children", children);
        return result;
    }

    /**
     * 删除节点
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteNode(@RequestParam String path) {
        zooKeeperService.deleteNode(path);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("path", path);
        result.put("message", "删除节点成功");
        return result;
    }
}
