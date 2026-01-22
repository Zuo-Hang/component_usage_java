package com.example.redis.service;

import com.example.redis.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis客户端使用示例
 * 使用 JedisUtil 工具类简化操作
 */
@Slf4j
@Service
public class JedisExampleService {

    @Autowired
    private JedisUtil jedisUtil;

    /**
     * String操作示例
     */
    public void stringOperations() {
        String key = "jedis:user:1001:name";
        String value = "张三";

        // 设置值
        jedisUtil.set(key, value);
        log.info("Jedis设置键值对: {} = {}", key, value);

        // 获取值
        String result = jedisUtil.get(key);
        log.info("Jedis获取值: {} = {}", key, result);

        // 设置过期时间（30秒）
        jedisUtil.set("jedis:user:1001:token", "abc123xyz", 30);
        log.info("Jedis设置带过期时间的键值对");

        // 递增操作
        jedisUtil.increment("jedis:counter:visits");
        Long count = jedisUtil.increment("jedis:counter:visits");
        log.info("Jedis计数器值: {}", count);
    }

    /**
     * Hash操作示例
     */
    public void hashOperations() {
        String key = "jedis:user:1001";

        // 设置Hash字段
        jedisUtil.hset(key, "name", "李四");
        jedisUtil.hset(key, "age", "25");
        jedisUtil.hset(key, "email", "lisi@example.com");
        log.info("Jedis设置Hash对象: {}", key);

        // 获取Hash字段
        String name = jedisUtil.hget(key, "name");
        log.info("Jedis获取Hash字段 name: {}", name);

        // 获取所有字段
        Map<String, String> hash = jedisUtil.hgetAll(key);
        log.info("Jedis获取所有Hash字段: {}", hash);
    }

    /**
     * List操作示例
     */
    public void listOperations() {
        String key = "jedis:task:queue";

        // 从左侧推入
        jedisUtil.lpush(key, "任务1", "任务2", "任务3");
        log.info("Jedis从左侧推入元素到List");

        // 从右侧弹出（队列模式）
        String task = jedisUtil.rpop(key);
        log.info("Jedis从右侧弹出任务: {}", task);

        // 获取List长度
        Long size = jedisUtil.llen(key);
        log.info("Jedis List长度: {}", size);

        // 获取指定范围的元素
        List<String> tasks = jedisUtil.lrangeAll(key);
        log.info("Jedis List所有元素: {}", tasks);
    }

    /**
     * Set操作示例
     */
    public void setOperations() {
        String key = "jedis:tags:article:1001";

        // 添加元素
        jedisUtil.sadd(key, "Java", "Redis", "Spring");
        log.info("Jedis添加元素到Set");

        // 判断元素是否存在
        Boolean exists = jedisUtil.sismember(key, "Java");
        log.info("Jedis元素'Java'是否存在: {}", exists);

        // 获取Set大小
        Long size = jedisUtil.scard(key);
        log.info("Jedis Set大小: {}", size);

        // 获取所有元素
        Set<String> members = jedisUtil.smembers(key);
        log.info("Jedis Set所有元素: {}", members);
    }

    /**
     * 分布式锁示例（使用SET命令的NX和EX参数）
     */
    public boolean tryLock(String lockKey, String lockValue, int expireSeconds) {
        boolean acquired = jedisUtil.tryLock(lockKey, lockValue, expireSeconds);
        log.info("Jedis尝试获取锁: {}, 结果: {}", lockKey, acquired);
        return acquired;
    }

    /**
     * 释放锁（需要验证值，避免误删其他客户端的锁）
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        boolean released = jedisUtil.releaseLock(lockKey, lockValue);
        log.info("Jedis释放锁: {}, 结果: {}", lockKey, released);
        return released;
    }

    /**
     * 管道操作示例（批量操作）
     */
    public void pipelineOperations() {
        // 使用工具类的批量操作方法
        Map<String, String> keyValues = new java.util.HashMap<>();
        keyValues.put("jedis:batch:key1", "value1");
        keyValues.put("jedis:batch:key2", "value2");
        keyValues.put("jedis:batch:key3", "value3");
        
        jedisUtil.batchSet(keyValues);
        jedisUtil.increment("jedis:batch:counter");
        
        log.info("Jedis批量操作完成");
    }

    /**
     * 事务操作示例
     * 注意：JedisUtil 未封装事务操作，这里保留原实现作为示例
     * 实际项目中建议使用 RedisTemplate 的事务支持或 Redisson 的事务功能
     */
    public void transactionOperations() {
        // 使用工具类进行批量操作模拟事务
        Map<String, String> keyValues = new java.util.HashMap<>();
        keyValues.put("jedis:tx:key1", "value1");
        keyValues.put("jedis:tx:key2", "value2");
        
        jedisUtil.batchSet(keyValues);
        jedisUtil.increment("jedis:tx:counter");
        
        log.info("Jedis批量操作（模拟事务）完成");
    }
}

