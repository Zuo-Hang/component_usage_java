package com.example.redis.service;

import com.example.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson使用示例服务类
 * 使用 RedissonUtil 工具类简化操作
 */
@Slf4j
@Service
public class RedissonExampleService {

    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * RBucket操作示例（相当于String类型，但支持对象存储）
     */
    public void bucketOperations() {
        // 设置值
        redissonUtil.set("user:1001:name", "张三");
        log.info("设置值: user:1001:name = 张三");

        // 获取值
        String value = redissonUtil.get("user:1001:name");
        log.info("获取值: {}", value);

        // 设置值并指定过期时间
        redissonUtil.set("user:1001:token", "abc123xyz", 30, TimeUnit.SECONDS);
        log.info("设置带过期时间的值");

        // 使用原子递增
        redissonUtil.getAtomicLong("counter:visits").set(0L);
        Long count = redissonUtil.increment("counter:visits");
        log.info("计数器值: {}", count);
    }

    /**
     * RMap操作示例（相当于Hash类型，但支持Java对象）
     */
    public void mapOperations() {
        String key = "user:1001";

        // 设置字段
        redissonUtil.hset(key, "name", "李四");
        redissonUtil.hset(key, "age", "25");
        redissonUtil.hset(key, "email", "lisi@example.com");
        log.info("设置Map对象: {}", key);

        // 获取字段
        String name = redissonUtil.hget(key, "name");
        log.info("获取Map字段 name: {}", name);

        // 获取所有字段
        Set<String> keys = redissonUtil.hkeys(key);
        log.info("Map的所有键: {}", keys);

        // 获取所有值
        List<String> values = redissonUtil.hvals(key);
        log.info("Map的所有值: {}", values);
    }

    /**
     * RList操作示例（List类型）
     */
    public void listOperations() {
        String key = "task:queue";

        // 从左侧推入
        redissonUtil.lpush(key, "任务1", "任务2", "任务3");
        log.info("从左侧推入元素到List");

        // 从右侧弹出（队列模式）
        String task = redissonUtil.rpop(key);
        log.info("从右侧弹出任务: {}", task);

        // 获取List长度
        int size = redissonUtil.llen(key);
        log.info("List长度: {}", size);

        // 获取所有元素
        List<String> tasks = redissonUtil.lrangeAll(key);
        log.info("List所有元素: {}", tasks);
    }

    /**
     * RSet操作示例（Set类型）
     */
    public void setOperations() {
        String key = "tags:article:1001";

        // 添加元素
        redissonUtil.sadd(key, "Java", "Redis", "Spring");
        log.info("添加元素到Set");

        // 判断元素是否存在
        boolean exists = redissonUtil.sismember(key, "Java");
        log.info("元素'Java'是否存在: {}", exists);

        // 获取Set大小
        int size = redissonUtil.scard(key);
        log.info("Set大小: {}", size);

        // 获取所有元素
        Set<String> allTags = redissonUtil.smembers(key);
        log.info("Set所有元素: {}", allTags);
    }

    /**
     * Redisson分布式锁示例（推荐方式 - 支持可重入、自动续期）
     */
    public boolean tryLockWithRedisson(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        // 尝试加锁，最多等待waitTime，锁过期时间leaseTime
        boolean acquired = redissonUtil.tryLock(lockKey, waitTime, leaseTime, timeUnit);
        if (acquired) {
            log.info("成功获取分布式锁: {}", lockKey);
            // 执行业务逻辑
            // ...
            // 释放锁
            redissonUtil.unlock(lockKey);
            log.info("释放分布式锁: {}", lockKey);
            return true;
        } else {
            log.warn("获取分布式锁失败: {}", lockKey);
            return false;
        }
    }

    /**
     * 公平锁示例
     */
    public boolean tryFairLock(String lockKey) {
        RLock fairLock = redissonUtil.getFairLock(lockKey);
        
        try {
            boolean acquired = fairLock.tryLock(10, 30, TimeUnit.SECONDS);
            if (acquired) {
                log.info("成功获取公平锁: {}", lockKey);
                // 业务逻辑
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (fairLock.isHeldByCurrentThread()) {
                fairLock.unlock();
            }
        }
    }

    /**
     * 读写锁示例
     */
    public void readWriteLockExample() {
        RReadWriteLock readWriteLock = redissonUtil.getReadWriteLock("readwrite:lock");
        
        // 读锁
        RLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            log.info("获取读锁，可以并发读取");
            // 读取操作
        } finally {
            readLock.unlock();
        }

        // 写锁
        RLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            log.info("获取写锁，独占写入");
            // 写入操作
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 信号量示例（限流）
     */
    public boolean trySemaphore(String semaphoreName, int permits) {
        RSemaphore semaphore = redissonUtil.getSemaphore(semaphoreName);
        
        try {
            // 初始化信号量
            semaphore.trySetPermits(permits);
            
            // 尝试获取许可
            boolean acquired = semaphore.tryAcquire(1, 1, TimeUnit.SECONDS);
            if (acquired) {
                log.info("获取信号量许可成功");
                // 执行业务逻辑
                // ...
                return true;
            } else {
                log.warn("获取信号量许可失败（已满）");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // 释放许可
            semaphore.release();
            log.info("释放信号量许可");
        }
    }

    /**
     * 布隆过滤器示例
     */
    public void bloomFilterExample() {
        RBloomFilter<String> bloomFilter = redissonUtil.getBloomFilter("bloom:filter");
        
        // 初始化布隆过滤器：期望插入数量、误差率
        bloomFilter.tryInit(10000L, 0.01);
        
        // 添加元素
        bloomFilter.add("user:1001");
        bloomFilter.add("user:1002");
        log.info("添加元素到布隆过滤器");

        // 检查元素是否存在
        boolean exists = bloomFilter.contains("user:1001");
        log.info("user:1001是否存在: {}", exists);

        boolean notExists = bloomFilter.contains("user:9999");
        log.info("user:9999是否存在: {}", notExists);
    }

    /**
     * 原子长整型示例
     */
    public void atomicLongExample() {
        String key = "atomic:counter";
        
        // 初始化值
        redissonUtil.getAtomicLong(key).set(0);
        
        // 原子递增
        long value = redissonUtil.increment(key);
        log.info("原子递增后的值: {}", value);
        
        // 原子加10
        long newValue = redissonUtil.getAtomicLong(key).addAndGet(10);
        log.info("原子加10后的值: {}", newValue);
    }
}

