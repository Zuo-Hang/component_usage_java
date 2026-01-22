package com.example.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis 工具类
 * 封装常见的 Redis 操作，提供简洁易用的 API
 */
@Slf4j
@Component
public class JedisUtil {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 获取 Jedis 连接（使用后需要关闭）
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    // ==================== String 操作 ====================

    /**
     * 设置键值对
     */
    public String set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            return jedis.set(key, value);
        }
    }

    /**
     * 设置键值对，带过期时间（秒）
     */
    public String set(String key, String value, int expireSeconds) {
        try (Jedis jedis = getJedis()) {
            SetParams params = new SetParams();
            params.ex(expireSeconds);
            return jedis.set(key, value, params);
        }
    }

    /**
     * 获取值
     */
    public String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    /**
     * 删除键
     */
    public Long delete(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.del(key);
        }
    }

    /**
     * 判断键是否存在
     */
    public Boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        }
    }

    /**
     * 设置过期时间（秒）
     */
    public Long expire(String key, int seconds) {
        try (Jedis jedis = getJedis()) {
            return jedis.expire(key, seconds);
        }
    }

    /**
     * 递增
     */
    public Long increment(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.incr(key);
        }
    }

    /**
     * 递增指定值
     */
    public Long incrementBy(String key, long increment) {
        try (Jedis jedis = getJedis()) {
            return jedis.incrBy(key, increment);
        }
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.decr(key);
        }
    }

    // ==================== Hash 操作 ====================

    /**
     * 设置 Hash 字段
     */
    public Long hset(String key, String field, String value) {
        try (Jedis jedis = getJedis()) {
            return jedis.hset(key, field, value);
        }
    }

    /**
     * 获取 Hash 字段值
     */
    public String hget(String key, String field) {
        try (Jedis jedis = getJedis()) {
            return jedis.hget(key, field);
        }
    }

    /**
     * 获取所有 Hash 字段和值
     */
    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.hgetAll(key);
        }
    }

    /**
     * 删除 Hash 字段
     */
    public Long hdel(String key, String... fields) {
        try (Jedis jedis = getJedis()) {
            return jedis.hdel(key, fields);
        }
    }

    /**
     * 判断 Hash 字段是否存在
     */
    public Boolean hexists(String key, String field) {
        try (Jedis jedis = getJedis()) {
            return jedis.hexists(key, field);
        }
    }

    /**
     * 获取 Hash 所有字段
     */
    public Set<String> hkeys(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.hkeys(key);
        }
    }

    /**
     * 获取 Hash 所有值
     */
    public List<String> hvals(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.hvals(key);
        }
    }

    // ==================== List 操作 ====================

    /**
     * 从左侧推入元素
     */
    public Long lpush(String key, String... values) {
        try (Jedis jedis = getJedis()) {
            return jedis.lpush(key, values);
        }
    }

    /**
     * 从右侧推入元素
     */
    public Long rpush(String key, String... values) {
        try (Jedis jedis = getJedis()) {
            return jedis.rpush(key, values);
        }
    }

    /**
     * 从左侧弹出元素
     */
    public String lpop(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.lpop(key);
        }
    }

    /**
     * 从右侧弹出元素
     */
    public String rpop(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.rpop(key);
        }
    }

    /**
     * 获取 List 长度
     */
    public Long llen(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.llen(key);
        }
    }

    /**
     * 获取 List 指定范围的元素
     */
    public List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = getJedis()) {
            return jedis.lrange(key, start, end);
        }
    }

    /**
     * 获取 List 所有元素
     */
    public List<String> lrangeAll(String key) {
        return lrange(key, 0, -1);
    }

    // ==================== Set 操作 ====================

    /**
     * 添加元素到 Set
     */
    public Long sadd(String key, String... members) {
        try (Jedis jedis = getJedis()) {
            return jedis.sadd(key, members);
        }
    }

    /**
     * 从 Set 移除元素
     */
    public Long srem(String key, String... members) {
        try (Jedis jedis = getJedis()) {
            return jedis.srem(key, members);
        }
    }

    /**
     * 判断元素是否在 Set 中
     */
    public Boolean sismember(String key, String member) {
        try (Jedis jedis = getJedis()) {
            return jedis.sismember(key, member);
        }
    }

    /**
     * 获取 Set 所有成员
     */
    public Set<String> smembers(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.smembers(key);
        }
    }

    /**
     * 获取 Set 大小
     */
    public Long scard(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.scard(key);
        }
    }

    // ==================== 分布式锁 ====================

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @param lockValue 锁的值（用于释放时验证）
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, int expireSeconds) {
        try (Jedis jedis = getJedis()) {
            SetParams params = new SetParams();
            params.nx(); // 只在键不存在时设置
            params.ex(expireSeconds); // 设置过期时间
            String result = jedis.set(lockKey, lockValue, params);
            return "OK".equals(result);
        }
    }

    /**
     * 释放分布式锁（使用 Lua 脚本确保原子性）
     * @param lockKey 锁的键
     * @param lockValue 锁的值（必须匹配才能释放）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        try (Jedis jedis = getJedis()) {
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                              "return redis.call('del', KEYS[1]) " +
                              "else return 0 end";
            Long result = (Long) jedis.eval(luaScript,
                                          java.util.Collections.singletonList(lockKey),
                                          java.util.Collections.singletonList(lockValue));
            return result != null && result == 1;
        }
    }

    // ==================== 批量操作 ====================

    /**
     * 批量设置键值对（使用管道）
     */
    public void batchSet(Map<String, String> keyValues) {
        try (Jedis jedis = getJedis()) {
            redis.clients.jedis.Pipeline pipeline = jedis.pipelined();
            keyValues.forEach(pipeline::set);
            pipeline.sync();
        }
    }

    /**
     * 批量获取值（使用管道）
     */
    public List<Object> batchGet(String... keys) {
        try (Jedis jedis = getJedis()) {
            redis.clients.jedis.Pipeline pipeline = jedis.pipelined();
            for (String key : keys) {
                pipeline.get(key);
            }
            return pipeline.syncAndReturnAll();
        }
    }
}
