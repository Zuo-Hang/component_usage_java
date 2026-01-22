package com.example.redis.controller;

import com.example.redis.service.RedissonExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Redis示例控制器（Redisson专用，兼容旧接口）
 * 新接口请使用 RedisClientExampleController
 */
@Slf4j
@RestController
@RequestMapping("/redis")
public class RedisExampleController {

    @Autowired
    private RedissonExampleService redisExampleService;

    @GetMapping("/bucket")
    public String testBucket() {
        redisExampleService.bucketOperations();
        return "Bucket操作示例执行完成，请查看日志";
    }

    @GetMapping("/map")
    public String testMap() {
        redisExampleService.mapOperations();
        return "Map操作示例执行完成，请查看日志";
    }

    @GetMapping("/list")
    public String testList() {
        redisExampleService.listOperations();
        return "List操作示例执行完成，请查看日志";
    }

    @GetMapping("/set")
    public String testSet() {
        redisExampleService.setOperations();
        return "Set操作示例执行完成，请查看日志";
    }

    @GetMapping("/lock")
    public String testLock() {
        String lockKey = "lock:order:1001";
        boolean acquired = redisExampleService.tryLockWithRedisson(
            lockKey, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
        return acquired ? "分布式锁示例：成功获取并释放锁" : "分布式锁示例：获取锁失败";
    }

    @GetMapping("/fairlock")
    public String testFairLock() {
        String lockKey = "fair:lock:order:1001";
        boolean acquired = redisExampleService.tryFairLock(lockKey);
        return acquired ? "公平锁示例：成功" : "公平锁示例：失败";
    }

    @GetMapping("/readwritelock")
    public String testReadWriteLock() {
        redisExampleService.readWriteLockExample();
        return "读写锁示例执行完成";
    }

    @GetMapping("/semaphore")
    public String testSemaphore() {
        boolean acquired = redisExampleService.trySemaphore("api:limit", 10);
        return acquired ? "信号量示例：获取许可成功" : "信号量示例：获取许可失败（已满）";
    }

    @GetMapping("/bloomfilter")
    public String testBloomFilter() {
        redisExampleService.bloomFilterExample();
        return "布隆过滤器示例执行完成，请查看日志";
    }

    @GetMapping("/atomic")
    public String testAtomicLong() {
        redisExampleService.atomicLongExample();
        return "原子长整型示例执行完成，请查看日志";
    }
}

