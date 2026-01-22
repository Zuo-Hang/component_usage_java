error id: file://<WORKSPACE>/redis-example/src/main/java/com/example/redis/service/RedisExampleService.java:_empty_/RedisTemplate#
file://<WORKSPACE>/redis-example/src/main/java/com/example/redis/service/RedisExampleService.java
empty definition using pc, found symbol in pc: _empty_/RedisTemplate#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 426
uri: file://<WORKSPACE>/redis-example/src/main/java/com/example/redis/service/RedisExampleService.java
text:
```scala
package com.example.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis使用示例服务类
 * 演示Redis的基本操作：String、Hash、List、Set等
 */
@Slf4j
@Service
public class RedisExampleService {

    @Autowired
    private RedisTe@@mplate<String, Object> redisTemplate;

    /**
     * String类型操作示例
     */
    public void stringOperations() {
        String key = "user:1001:name";
        String value = "张三";

        // 设置值
        redisTemplate.opsForValue().set(key, value);
        log.info("设置键值对: {} = {}", key, value);

        // 获取值
        Object result = redisTemplate.opsForValue().get(key);
        log.info("获取值: {} = {}", key, result);

        // 设置过期时间（30秒）
        redisTemplate.opsForValue().set("user:1001:token", "abc123xyz", 30, TimeUnit.SECONDS);
        log.info("设置带过期时间的键值对");

        // 递增操作
        redisTemplate.opsForValue().increment("counter:visits");
        Long count = redisTemplate.opsForValue().increment("counter:visits");
        log.info("计数器值: {}", count);
    }

    /**
     * Hash类型操作示例
     */
    public void hashOperations() {
        String key = "user:1001";

        // 设置Hash字段
        redisTemplate.opsForHash().put(key, "name", "李四");
        redisTemplate.opsForHash().put(key, "age", "25");
        redisTemplate.opsForHash().put(key, "email", "lisi@example.com");
        log.info("设置Hash对象: {}", key);

        // 获取Hash字段
        Object name = redisTemplate.opsForHash().get(key, "name");
        log.info("获取Hash字段 name: {}", name);

        // 获取所有字段
        Object hash = redisTemplate.opsForHash().entries(key);
        log.info("获取所有Hash字段: {}", hash);
    }

    /**
     * List类型操作示例
     */
    public void listOperations() {
        String key = "task:queue";

        // 从左侧推入
        redisTemplate.opsForList().leftPush(key, "任务1");
        redisTemplate.opsForList().leftPush(key, "任务2");
        redisTemplate.opsForList().leftPush(key, "任务3");
        log.info("从左侧推入元素到List");

        // 从右侧弹出（队列模式）
        Object task = redisTemplate.opsForList().rightPop(key);
        log.info("从右侧弹出任务: {}", task);

        // 获取List长度
        Long size = redisTemplate.opsForList().size(key);
        log.info("List长度: {}", size);
    }

    /**
     * Set类型操作示例
     */
    public void setOperations() {
        String key = "tags:article:1001";

        // 添加元素
        redisTemplate.opsForSet().add(key, "Java", "Redis", "Spring");
        log.info("添加元素到Set");

        // 判断元素是否存在
        Boolean exists = redisTemplate.opsForSet().isMember(key, "Java");
        log.info("元素'Java'是否存在: {}", exists);

        // 获取Set大小
        Long size = redisTemplate.opsForSet().size(key);
        log.info("Set大小: {}", size);
    }

    /**
     * 分布式锁示例（简单实现）
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTime, TimeUnit timeUnit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, timeUnit);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 释放锁
     */
    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
        log.info("释放锁: {}", lockKey);
    }
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/RedisTemplate#