package com.example.redis.controller;

import com.example.redis.service.JedisExampleService;
import com.example.redis.service.LettuceExampleService;
import com.example.redis.service.RedissonExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis客户端示例控制器
 * 展示三种客户端（Jedis、Lettuce、Redisson）的使用方式
 */
@Slf4j
@RestController
@RequestMapping("/redis/client")
public class RedisClientExampleController {

    @Autowired
    private JedisExampleService jedisExampleService;

    @Autowired
    private LettuceExampleService lettuceExampleService;

    @Autowired
    private RedissonExampleService redissonExampleService;

    // ==================== Jedis示例 ====================

    @GetMapping("/jedis/string")
    public String jedisString() {
        jedisExampleService.stringOperations();
        return "Jedis String操作示例执行完成，请查看日志";
    }

    @GetMapping("/jedis/hash")
    public String jedisHash() {
        jedisExampleService.hashOperations();
        return "Jedis Hash操作示例执行完成，请查看日志";
    }

    @GetMapping("/jedis/list")
    public String jedisList() {
        jedisExampleService.listOperations();
        return "Jedis List操作示例执行完成，请查看日志";
    }

    @GetMapping("/jedis/set")
    public String jedisSet() {
        jedisExampleService.setOperations();
        return "Jedis Set操作示例执行完成，请查看日志";
    }

    @GetMapping("/jedis/lock")
    public String jedisLock() {
        String lockKey = "jedis:lock:order:1001";
        String lockValue = "request-" + System.currentTimeMillis();
        boolean acquired = jedisExampleService.tryLock(lockKey, lockValue, 10);
        if (acquired) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            jedisExampleService.releaseLock(lockKey, lockValue);
            return "Jedis分布式锁示例：成功获取并释放锁";
        }
        return "Jedis分布式锁示例：获取锁失败";
    }

    @GetMapping("/jedis/pipeline")
    public String jedisPipeline() {
        jedisExampleService.pipelineOperations();
        return "Jedis管道操作示例执行完成，请查看日志";
    }

    @GetMapping("/jedis/transaction")
    public String jedisTransaction() {
        jedisExampleService.transactionOperations();
        return "Jedis事务操作示例执行完成，请查看日志";
    }

    // ==================== Lettuce示例 ====================

    @GetMapping("/lettuce/string")
    public String lettuceString() {
        lettuceExampleService.stringOperations();
        return "Lettuce同步String操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/hash")
    public String lettuceHash() {
        lettuceExampleService.hashOperations();
        return "Lettuce同步Hash操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/list")
    public String lettuceList() {
        lettuceExampleService.listOperations();
        return "Lettuce同步List操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/set")
    public String lettuceSet() {
        lettuceExampleService.setOperations();
        return "Lettuce同步Set操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/async")
    public String lettuceAsync() {
        lettuceExampleService.asyncOperations();
        try {
            Thread.sleep(2000); // 等待异步操作完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Lettuce异步操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/reactive")
    public String lettuceReactive() {
        lettuceExampleService.reactiveOperations();
        try {
            Thread.sleep(2000); // 等待反应式操作完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Lettuce反应式操作示例执行完成，请查看日志";
    }

    @GetMapping("/lettuce/lock")
    public String lettuceLock() {
        String lockKey = "lettuce:lock:order:1001";
        String lockValue = "request-" + System.currentTimeMillis();
        boolean acquired = lettuceExampleService.tryLock(lockKey, lockValue, 10);
        if (acquired) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            lettuceExampleService.releaseLock(lockKey, lockValue);
            return "Lettuce分布式锁示例：成功获取并释放锁";
        }
        return "Lettuce分布式锁示例：获取锁失败";
    }

    @GetMapping("/lettuce/batch")
    public String lettuceBatch() {
        lettuceExampleService.pipelineOperations();
        return "Lettuce批量操作示例执行完成，请查看日志";
    }

    // ==================== Redisson示例 ====================

    @GetMapping("/redisson/bucket")
    public String redissonBucket() {
        redissonExampleService.bucketOperations();
        return "Redisson Bucket操作示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/map")
    public String redissonMap() {
        redissonExampleService.mapOperations();
        return "Redisson Map操作示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/list")
    public String redissonList() {
        redissonExampleService.listOperations();
        return "Redisson List操作示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/set")
    public String redissonSet() {
        redissonExampleService.setOperations();
        return "Redisson Set操作示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/lock")
    public String redissonLock() {
        String lockKey = "redisson:lock:order:1001";
        boolean acquired = redissonExampleService.tryLockWithRedisson(
            lockKey, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
        return acquired ? "Redisson分布式锁示例：成功获取并释放锁" : "Redisson分布式锁示例：获取锁失败";
    }

    @GetMapping("/redisson/fairlock")
    public String redissonFairLock() {
        String lockKey = "redisson:fair:lock:order:1001";
        boolean acquired = redissonExampleService.tryFairLock(lockKey);
        return acquired ? "Redisson公平锁示例：成功" : "Redisson公平锁示例：失败";
    }

    @GetMapping("/redisson/readwritelock")
    public String redissonReadWriteLock() {
        redissonExampleService.readWriteLockExample();
        return "Redisson读写锁示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/semaphore")
    public String redissonSemaphore() {
        boolean acquired = redissonExampleService.trySemaphore("redisson:api:limit", 10);
        return acquired ? "Redisson信号量示例：获取许可成功" : "Redisson信号量示例：获取许可失败（已满）";
    }

    @GetMapping("/redisson/bloomfilter")
    public String redissonBloomFilter() {
        redissonExampleService.bloomFilterExample();
        return "Redisson布隆过滤器示例执行完成，请查看日志";
    }

    @GetMapping("/redisson/atomic")
    public String redissonAtomic() {
        redissonExampleService.atomicLongExample();
        return "Redisson原子长整型示例执行完成，请查看日志";
    }

    // ==================== 对比示例 ====================

    @GetMapping("/compare/string")
    public Map<String, String> compareString() {
        Map<String, String> result = new HashMap<>();
        result.put("jedis", "调用 /redis/client/jedis/string");
        result.put("lettuce", "调用 /redis/client/lettuce/string");
        result.put("redisson", "调用 /redis/client/redisson/bucket");
        result.put("说明", "三种客户端都支持String操作，Redisson使用RBucket更优雅");
        return result;
    }

    @GetMapping("/compare/lock")
    public Map<String, String> compareLock() {
        Map<String, String> result = new HashMap<>();
        result.put("jedis", "需要手动实现SET NX EX，需要Lua脚本释放锁");
        result.put("lettuce", "使用SET NX EX，需要Lua脚本释放锁");
        result.put("redisson", "内置RLock，支持可重入、自动续期，使用更简单");
        result.put("推荐", "Redisson的分布式锁功能最强大");
        return result;
    }
}

