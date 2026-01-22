package com.example.redis.controller;

import com.example.redis.util.JedisUtil;
import com.example.redis.util.LettuceUtil;
import com.example.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 工具类使用示例控制器
 * 展示如何使用 JedisUtil、LettuceUtil、RedissonUtil
 */
@Slf4j
@RestController
@RequestMapping("/redis/util")
public class RedisUtilController {

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private LettuceUtil lettuceUtil;

    @Autowired
    private RedissonUtil redissonUtil;

    // ==================== JedisUtil 示例 ====================

    @GetMapping("/jedis/set")
    public String jedisSet(@RequestParam String key, @RequestParam String value) {
        jedisUtil.set(key, value);
        return "JedisUtil 设置成功: " + key + " = " + value;
    }

    @GetMapping("/jedis/get")
    public String jedisGet(@RequestParam String key) {
        String value = jedisUtil.get(key);
        return "JedisUtil 获取值: " + key + " = " + value;
    }

    @GetMapping("/jedis/setex")
    public String jedisSetex(@RequestParam String key, 
                             @RequestParam String value,
                             @RequestParam(defaultValue = "60") int seconds) {
        jedisUtil.set(key, value, seconds);
        return "JedisUtil 设置带过期时间: " + key + " = " + value + ", 过期时间: " + seconds + "秒";
    }

    @GetMapping("/jedis/hset")
    public String jedisHset(@RequestParam String key,
                            @RequestParam String field,
                            @RequestParam String value) {
        jedisUtil.hset(key, field, value);
        return "JedisUtil Hash设置成功: " + key + "." + field + " = " + value;
    }

    @GetMapping("/jedis/hget")
    public String jedisHget(@RequestParam String key, @RequestParam String field) {
        String value = jedisUtil.hget(key, field);
        return "JedisUtil Hash获取值: " + key + "." + field + " = " + value;
    }

    @GetMapping("/jedis/lock")
    public String jedisLock(@RequestParam(defaultValue = "util:lock:test") String lockKey) {
        String lockValue = "request-" + System.currentTimeMillis();
        boolean acquired = jedisUtil.tryLock(lockKey, lockValue, 10);
        if (acquired) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            jedisUtil.releaseLock(lockKey, lockValue);
            return "JedisUtil 分布式锁: 获取并释放成功";
        }
        return "JedisUtil 分布式锁: 获取失败";
    }

    // ==================== LettuceUtil 示例 ====================

    @GetMapping("/lettuce/set")
    public String lettuceSet(@RequestParam String key, @RequestParam String value) {
        lettuceUtil.set(key, value);
        return "LettuceUtil 设置成功: " + key + " = " + value;
    }

    @GetMapping("/lettuce/get")
    public String lettuceGet(@RequestParam String key) {
        String value = lettuceUtil.get(key);
        return "LettuceUtil 获取值: " + key + " = " + value;
    }

    @GetMapping("/lettuce/setex")
    public String lettuceSetex(@RequestParam String key,
                              @RequestParam String value,
                              @RequestParam(defaultValue = "60") int seconds) {
        lettuceUtil.setex(key, seconds, value);
        return "LettuceUtil 设置带过期时间: " + key + " = " + value + ", 过期时间: " + seconds + "秒";
    }

    @GetMapping("/lettuce/async")
    public String lettuceAsync(@RequestParam String key, @RequestParam String value) {
        lettuceUtil.setAsync(key, value)
                .thenAccept(result -> log.info("LettuceUtil 异步设置完成: {}", result))
                .exceptionally(throwable -> {
                    log.error("LettuceUtil 异步操作失败", throwable);
                    return null;
                });
        return "LettuceUtil 异步操作已提交，请查看日志";
    }

    @GetMapping("/lettuce/lock")
    public String lettuceLock(@RequestParam(defaultValue = "util:lock:test") String lockKey) {
        String lockValue = "request-" + System.currentTimeMillis();
        boolean acquired = lettuceUtil.tryLock(lockKey, lockValue, 10);
        if (acquired) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            lettuceUtil.releaseLock(lockKey, lockValue);
            return "LettuceUtil 分布式锁: 获取并释放成功";
        }
        return "LettuceUtil 分布式锁: 获取失败";
    }

    // ==================== RedissonUtil 示例 ====================

    @GetMapping("/redisson/set")
    public String redissonSet(@RequestParam String key, @RequestParam String value) {
        redissonUtil.set(key, value);
        return "RedissonUtil 设置成功: " + key + " = " + value;
    }

    @GetMapping("/redisson/get")
    public String redissonGet(@RequestParam String key) {
        String value = redissonUtil.get(key);
        return "RedissonUtil 获取值: " + key + " = " + value;
    }

    @GetMapping("/redisson/setex")
    public String redissonSetex(@RequestParam String key,
                               @RequestParam String value,
                               @RequestParam(defaultValue = "60") long seconds) {
        redissonUtil.set(key, value, seconds, java.util.concurrent.TimeUnit.SECONDS);
        return "RedissonUtil 设置带过期时间: " + key + " = " + value + ", 过期时间: " + seconds + "秒";
    }

    @GetMapping("/redisson/hset")
    public String redissonHset(@RequestParam String key,
                              @RequestParam String field,
                              @RequestParam String value) {
        redissonUtil.hset(key, field, value);
        return "RedissonUtil Hash设置成功: " + key + "." + field + " = " + value;
    }

    @GetMapping("/redisson/hget")
    public String redissonHget(@RequestParam String key, @RequestParam String field) {
        String value = redissonUtil.hget(key, field);
        return "RedissonUtil Hash获取值: " + key + "." + field + " = " + value;
    }

    @GetMapping("/redisson/lock")
    public String redissonLock(@RequestParam(defaultValue = "util:lock:test") String lockKey) {
        boolean acquired = redissonUtil.tryLock(lockKey, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
        if (acquired) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            redissonUtil.unlock(lockKey);
            return "RedissonUtil 分布式锁: 获取并释放成功";
        }
        return "RedissonUtil 分布式锁: 获取失败";
    }

    @GetMapping("/redisson/increment")
    public String redissonIncrement(@RequestParam String key) {
        long value = redissonUtil.increment(key);
        return "RedissonUtil 原子递增: " + key + " = " + value;
    }

    // ==================== 工具类对比 ====================

    @GetMapping("/compare/set")
    public Map<String, String> compareSet() {
        Map<String, String> result = new HashMap<>();
        String key = "util:compare:test";
        String value = "test_value";

        // Jedis
        jedisUtil.set(key + ":jedis", value);
        result.put("Jedis", "jedisUtil.set(key, value)");

        // Lettuce
        lettuceUtil.set(key + ":lettuce", value);
        result.put("Lettuce", "lettuceUtil.set(key, value)");

        // Redisson
        redissonUtil.set(key + ":redisson", value);
        result.put("Redisson", "redissonUtil.set(key, value)");

        result.put("说明", "三种工具类的 API 基本一致，使用方式相同");
        return result;
    }

    @GetMapping("/compare/lock")
    public Map<String, String> compareLock() {
        Map<String, String> result = new HashMap<>();
        result.put("Jedis", "jedisUtil.tryLock(key, value, seconds) - 需要手动实现");
        result.put("Lettuce", "lettuceUtil.tryLock(key, value, seconds) - 需要手动实现");
        result.put("Redisson", "redissonUtil.tryLock(key, waitTime, leaseTime, timeUnit) - 内置支持，功能最强大");
        result.put("推荐", "Redisson 的分布式锁支持可重入、自动续期等高级功能");
        return result;
    }

    // ==================== Redis Key 覆盖行为演示 ====================

    /**
     * 演示 Redis 重复设置 key 的行为
     * 说明：
     * 1. String 类型：SET 操作会直接覆盖旧值
     * 2. Hash 类型：HSET 只更新指定字段，不影响其他字段
     * 3. List 类型：LPUSH/RPUSH 会追加，但直接 SET 会覆盖整个 List
     * 4. Set 类型：SADD 会添加元素，不会覆盖
     */
    @GetMapping("/demo/overwrite")
    public Map<String, Object> demoOverwrite() {
        Map<String, Object> result = new HashMap<>();
        
        // ========== 1. String 类型：直接覆盖 ==========
        String stringKey = "demo:string:test";
        jedisUtil.set(stringKey, "原始值");
        String oldValue = jedisUtil.get(stringKey);
        jedisUtil.set(stringKey, "新值");
        String newValue = jedisUtil.get(stringKey);
        
        Map<String, String> stringDemo = new HashMap<>();
        stringDemo.put("第一次设置", oldValue);
        stringDemo.put("第二次设置（覆盖）", newValue);
        stringDemo.put("结论", "String 类型：SET 操作会直接覆盖旧值，旧值被完全替换");
        result.put("String 类型演示", stringDemo);
        
        // ========== 2. Hash 类型：只更新指定字段 ==========
        String hashKey = "demo:hash:test";
        jedisUtil.hset(hashKey, "name", "张三");
        jedisUtil.hset(hashKey, "age", "25");
        jedisUtil.hset(hashKey, "email", "zhangsan@example.com");
        Map<String, String> hashBefore = jedisUtil.hgetAll(hashKey);
        
        // 只更新 name 字段
        jedisUtil.hset(hashKey, "name", "李四");
        Map<String, String> hashAfter = jedisUtil.hgetAll(hashKey);
        
        Map<String, Object> hashDemo = new HashMap<>();
        hashDemo.put("第一次设置所有字段", hashBefore);
        hashDemo.put("只更新 name 字段后", hashAfter);
        hashDemo.put("结论", "Hash 类型：HSET 只更新指定字段，其他字段保持不变");
        result.put("Hash 类型演示", hashDemo);
        
        // ========== 3. List 类型：追加 vs 覆盖 ==========
        String listKey = "demo:list:test";
        jedisUtil.lpush(listKey, "元素1", "元素2", "元素3");
        java.util.List<String> listBefore = jedisUtil.lrangeAll(listKey);
        
        // 再次 LPUSH 会追加到列表头部
        jedisUtil.lpush(listKey, "新元素");
        java.util.List<String> listAfterPush = jedisUtil.lrangeAll(listKey);
        
        Map<String, Object> listDemo = new HashMap<>();
        listDemo.put("第一次 LPUSH", listBefore);
        listDemo.put("第二次 LPUSH（追加）", listAfterPush);
        listDemo.put("结论", "List 类型：LPUSH/RPUSH 会追加元素，不会覆盖。但如果使用 SET 命令会覆盖整个 List");
        result.put("List 类型演示", listDemo);
        
        // ========== 4. Set 类型：添加元素 ==========
        String setKey = "demo:set:test";
        jedisUtil.sadd(setKey, "Java", "Redis", "Spring");
        java.util.Set<String> setBefore = jedisUtil.smembers(setKey);
        
        // 再次 SADD 会添加新元素
        jedisUtil.sadd(setKey, "MyBatis", "Java"); // Java 已存在，不会重复
        java.util.Set<String> setAfter = jedisUtil.smembers(setKey);
        
        Map<String, Object> setDemo = new HashMap<>();
        setDemo.put("第一次 SADD", setBefore);
        setDemo.put("第二次 SADD（添加）", setAfter);
        setDemo.put("结论", "Set 类型：SADD 会添加新元素，已存在的元素不会重复，不会覆盖整个 Set");
        result.put("Set 类型演示", setDemo);
        
        // ========== 总结 ==========
        Map<String, String> summary = new HashMap<>();
        summary.put("String", "SET 命令会完全覆盖旧值");
        summary.put("Hash", "HSET 只更新指定字段，不影响其他字段");
        summary.put("List", "LPUSH/RPUSH 追加元素，但 SET 会覆盖整个 List");
        summary.put("Set", "SADD 添加元素，不会覆盖，已存在元素不会重复");
        summary.put("重要提示", "Redis 不会自动删除旧内容，但 SET 操作会直接替换整个值");
        result.put("总结", summary);
        
        return result;
    }
}
