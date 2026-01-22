package com.example.redis.service;

import com.example.redis.util.LettuceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Lettuce客户端使用示例
 * 使用 LettuceUtil 工具类简化操作
 */
@Slf4j
@Service
public class LettuceExampleService {

    @Autowired
    private LettuceUtil lettuceUtil;

    /**
     * 同步操作示例 - String
     */
    public void stringOperations() {
        String key = "lettuce:user:1001:name";
        String value = "张三";

        // 设置值
        lettuceUtil.set(key, value);
        log.info("Lettuce同步设置键值对: {} = {}", key, value);

        // 获取值
        String result = lettuceUtil.get(key);
        log.info("Lettuce同步获取值: {} = {}", key, result);

        // 设置过期时间
        lettuceUtil.setex("lettuce:user:1001:token", 30, "abc123xyz");
        log.info("Lettuce同步设置带过期时间的键值对");

        // 递增操作
        lettuceUtil.increment("lettuce:counter:visits");
        Long count = lettuceUtil.increment("lettuce:counter:visits");
        log.info("Lettuce同步计数器值: {}", count);
    }

    /**
     * 同步操作示例 - Hash
     */
    public void hashOperations() {
        String key = "lettuce:user:1001";

        // 设置Hash字段
        lettuceUtil.hset(key, "name", "李四");
        lettuceUtil.hset(key, "age", "25");
        lettuceUtil.hset(key, "email", "lisi@example.com");
        log.info("Lettuce同步设置Hash对象: {}", key);

        // 获取Hash字段
        String name = lettuceUtil.hget(key, "name");
        log.info("Lettuce同步获取Hash字段 name: {}", name);

        // 获取所有字段
        Map<String, String> hash = lettuceUtil.hgetAll(key);
        log.info("Lettuce同步获取所有Hash字段: {}", hash);
    }

    /**
     * 同步操作示例 - List
     */
    public void listOperations() {
        String key = "lettuce:task:queue";

        // 从左侧推入
        lettuceUtil.lpush(key, "任务1", "任务2", "任务3");
        log.info("Lettuce同步从左侧推入元素到List");

        // 从右侧弹出
        String task = lettuceUtil.rpop(key);
        log.info("Lettuce同步从右侧弹出任务: {}", task);

        // 获取List长度
        Long size = lettuceUtil.llen(key);
        log.info("Lettuce同步List长度: {}", size);

        // 获取指定范围的元素
        List<String> tasks = lettuceUtil.lrangeAll(key);
        log.info("Lettuce同步List所有元素: {}", tasks);
    }

    /**
     * 同步操作示例 - Set
     */
    public void setOperations() {
        String key = "lettuce:tags:article:1001";

        // 添加元素
        lettuceUtil.sadd(key, "Java", "Redis", "Spring");
        log.info("Lettuce同步添加元素到Set");

        // 判断元素是否存在
        Boolean exists = lettuceUtil.sismember(key, "Java");
        log.info("Lettuce同步元素'Java'是否存在: {}", exists);

        // 获取Set大小
        Long size = lettuceUtil.scard(key);
        log.info("Lettuce同步Set大小: {}", size);

        // 获取所有元素
        Set<String> members = lettuceUtil.smembers(key);
        log.info("Lettuce同步Set所有元素: {}", members);
    }

    /**
     * 异步操作示例
     */
    public void asyncOperations() {
        String key = "lettuce:async:test";

        // 异步设置值
        CompletableFuture<String> setFuture = lettuceUtil.setAsync(key, "async_value");
        
        setFuture.thenAccept(result -> {
            log.info("Lettuce异步设置完成: {}", result);
            
            // 异步获取值
            CompletableFuture<String> getFuture = lettuceUtil.getAsync(key);
            getFuture.thenAccept(value -> {
                log.info("Lettuce异步获取值: {}", value);
            }).exceptionally(throwable -> {
                log.error("Lettuce异步操作失败", throwable);
                return null;
            });
        });
        
        log.info("Lettuce异步操作已提交");
    }

    /**
     * 反应式操作示例（基于Reactor）
     * 注意：LettuceUtil 未封装反应式操作，这里保留注释说明
     * 实际项目中如需使用反应式操作，可以直接使用 Lettuce 的 reactiveCommands
     */
    public void reactiveOperations() {
        String key = "lettuce:reactive:test";

        // 使用异步操作模拟反应式
        CompletableFuture<String> setFuture = lettuceUtil.setAsync(key, "reactive_value");
        
        setFuture.thenCompose(result -> {
            log.info("Lettuce异步设置完成: {}", result);
            return lettuceUtil.getAsync(key);
        }).thenAccept(value -> {
            log.info("Lettuce异步获取值: {}", value);
            log.info("Lettuce操作完成");
        }).exceptionally(throwable -> {
            log.error("Lettuce操作失败", throwable);
            return null;
        });
        
        log.info("Lettuce异步操作已提交");
    }

    /**
     * 分布式锁示例（使用SET命令）
     */
    public boolean tryLock(String lockKey, String lockValue, long expireSeconds) {
        boolean acquired = lettuceUtil.tryLock(lockKey, lockValue, (int) expireSeconds);
        log.info("Lettuce尝试获取锁: {}, 结果: {}", lockKey, acquired);
        return acquired;
    }

    /**
     * 释放锁（使用Lua脚本）
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        boolean released = lettuceUtil.releaseLock(lockKey, lockValue);
        log.info("Lettuce释放锁: {}, 结果: {}", lockKey, released);
        return released;
    }

    /**
     * 批量操作示例
     */
    public void pipelineOperations() {
        // 使用工具类的批量操作方法
        java.util.Map<String, String> keyValues = new java.util.HashMap<>();
        keyValues.put("lettuce:batch:key1", "value1");
        keyValues.put("lettuce:batch:key2", "value2");
        keyValues.put("lettuce:batch:key3", "value3");
        
        lettuceUtil.batchSet(keyValues);
        lettuceUtil.increment("lettuce:batch:counter");
        
        log.info("Lettuce批量操作已完成");
    }
}

