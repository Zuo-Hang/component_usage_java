package com.example.redis.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Lettuce 工具类
 * 封装常见的 Redis 操作，支持同步和异步操作
 */
@Slf4j
@Component
public class LettuceUtil {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;
    private RedisAsyncCommands<String, String> asyncCommands;

    @PostConstruct
    public void init() {
        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            uriBuilder.withPassword(redisPassword.toCharArray());
        }
        
        RedisURI redisUri = uriBuilder.build();
        redisClient = RedisClient.create(redisUri);
        connection = redisClient.connect();
        syncCommands = connection.sync();
        asyncCommands = connection.async();
        
        log.info("LettuceUtil 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        log.info("LettuceUtil 已关闭");
    }

    // ==================== String 操作（同步）====================

    /**
     * 设置键值对
     */
    public String set(String key, String value) {
        return syncCommands.set(key, value);
    }

    /**
     * 设置键值对，带过期时间（秒）
     */
    public String setex(String key, int seconds, String value) {
        return syncCommands.setex(key, seconds, value);
    }

    /**
     * 获取值
     */
    public String get(String key) {
        return syncCommands.get(key);
    }

    /**
     * 删除键
     */
    public Long delete(String key) {
        return syncCommands.del(key);
    }

    /**
     * 判断键是否存在
     */
    public Boolean exists(String key) {
        return syncCommands.exists(key) > 0;
    }

    /**
     * 设置过期时间（秒）
     */
    public Boolean expire(String key, int seconds) {
        return syncCommands.expire(key, Duration.ofSeconds(seconds));
    }

    /**
     * 递增
     */
    public Long increment(String key) {
        return syncCommands.incr(key);
    }

    /**
     * 递增指定值
     */
    public Long incrementBy(String key, long increment) {
        return syncCommands.incrby(key, increment);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return syncCommands.decr(key);
    }

    // ==================== Hash 操作（同步）====================

    /**
     * 设置 Hash 字段
     */
    public Boolean hset(String key, String field, String value) {
        return syncCommands.hset(key, field, value);
    }

    /**
     * 获取 Hash 字段值
     */
    public String hget(String key, String field) {
        return syncCommands.hget(key, field);
    }

    /**
     * 获取所有 Hash 字段和值
     */
    public Map<String, String> hgetAll(String key) {
        return syncCommands.hgetall(key);
    }

    /**
     * 删除 Hash 字段
     */
    public Long hdel(String key, String... fields) {
        return syncCommands.hdel(key, fields);
    }

    /**
     * 判断 Hash 字段是否存在
     */
    public Boolean hexists(String key, String field) {
        return syncCommands.hexists(key, field);
    }

    /**
     * 获取 Hash 所有字段
     */
    public Set<String> hkeys(String key) {
        return new java.util.HashSet<>(syncCommands.hkeys(key));
    }

    /**
     * 获取 Hash 所有值
     */
    public List<String> hvals(String key) {
        return new java.util.ArrayList<>(syncCommands.hvals(key));
    }

    // ==================== List 操作（同步）====================

    /**
     * 从左侧推入元素
     */
    public Long lpush(String key, String... values) {
        return syncCommands.lpush(key, values);
    }

    /**
     * 从右侧推入元素
     */
    public Long rpush(String key, String... values) {
        return syncCommands.rpush(key, values);
    }

    /**
     * 从左侧弹出元素
     */
    public String lpop(String key) {
        return syncCommands.lpop(key);
    }

    /**
     * 从右侧弹出元素
     */
    public String rpop(String key) {
        return syncCommands.rpop(key);
    }

    /**
     * 获取 List 长度
     */
    public Long llen(String key) {
        return syncCommands.llen(key);
    }

    /**
     * 获取 List 指定范围的元素
     */
    public List<String> lrange(String key, long start, long end) {
        return syncCommands.lrange(key, start, end);
    }

    /**
     * 获取 List 所有元素
     */
    public List<String> lrangeAll(String key) {
        return lrange(key, 0, -1);
    }

    // ==================== Set 操作（同步）====================

    /**
     * 添加元素到 Set
     */
    public Long sadd(String key, String... members) {
        return syncCommands.sadd(key, members);
    }

    /**
     * 从 Set 移除元素
     */
    public Long srem(String key, String... members) {
        return syncCommands.srem(key, members);
    }

    /**
     * 判断元素是否在 Set 中
     */
    public Boolean sismember(String key, String member) {
        return syncCommands.sismember(key, member);
    }

    /**
     * 获取 Set 所有成员
     */
    public Set<String> smembers(String key) {
        return syncCommands.smembers(key);
    }

    /**
     * 获取 Set 大小
     */
    public Long scard(String key) {
        return syncCommands.scard(key);
    }

    // ==================== 异步操作 ====================

    /**
     * 异步设置键值对
     */
    public CompletableFuture<String> setAsync(String key, String value) {
        return asyncCommands.set(key, value).toCompletableFuture();
    }

    /**
     * 异步获取值
     */
    public CompletableFuture<String> getAsync(String key) {
        return asyncCommands.get(key).toCompletableFuture();
    }

    /**
     * 异步删除键
     */
    public CompletableFuture<Long> deleteAsync(String key) {
        return asyncCommands.del(key).toCompletableFuture();
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, int expireSeconds) {
        String result = syncCommands.set(lockKey, lockValue,
                io.lettuce.core.SetArgs.Builder.nx().ex(Duration.ofSeconds(expireSeconds)));
        return "OK".equals(result);
    }

    /**
     * 释放分布式锁（使用 Lua 脚本）
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                          "return redis.call('del', KEYS[1]) " +
                          "else return 0 end";
        Long result = syncCommands.eval(luaScript,
                                       io.lettuce.core.ScriptOutputType.INTEGER,
                                       new String[]{lockKey}, lockValue);
        return result != null && result == 1;
    }

    // ==================== 批量操作 ====================

    /**
     * 批量设置（使用管道）
     */
    public void batchSet(Map<String, String> keyValues) {
        asyncCommands.setAutoFlushCommands(false);
        keyValues.forEach((key, value) -> asyncCommands.set(key, value));
        asyncCommands.flushCommands();
        asyncCommands.setAutoFlushCommands(true);
    }
}
