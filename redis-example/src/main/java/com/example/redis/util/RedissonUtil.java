package com.example.redis.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 工具类
 * 封装常见的 Redis 操作，使用 Redisson 的高级功能
 */
@Slf4j
@Component
public class RedissonUtil {

    @Autowired
    private RedissonClient redissonClient;

    // ==================== Bucket (String) 操作 ====================

    /**
     * 设置键值对
     */
    public <T> void set(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    /**
     * 设置键值对，带过期时间
     */
    public <T> void set(String key, T value, long time, TimeUnit timeUnit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value);
        bucket.expire(time, timeUnit);
    }

    /**
     * 获取值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 删除键
     */
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 判断键是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        return redissonClient.getBucket(key).expire(time, timeUnit);
    }

    // ==================== Map (Hash) 操作 ====================

    /**
     * 设置 Hash 字段
     */
    public <K, V> void hset(String key, K field, V value) {
        RMap<K, V> map = redissonClient.getMap(key);
        map.put(field, value);
    }

    /**
     * 获取 Hash 字段值
     */
    @SuppressWarnings("unchecked")
    public <K, V> V hget(String key, K field) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.get(field);
    }

    /**
     * 获取所有 Hash 字段和值
     */
    @SuppressWarnings("unchecked")
    public <K, V> java.util.Map<K, V> hgetAll(String key) {
        RMap<K, V> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    /**
     * 删除 Hash 字段
     */
    public <K> boolean hdel(String key, K field) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.remove(field) != null;
    }

    /**
     * 判断 Hash 字段是否存在
     */
    public <K> boolean hexists(String key, K field) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        return map.containsKey(field);
    }

    /**
     * 获取 Hash 所有字段
     */
    @SuppressWarnings("unchecked")
    public <K> Set<K> hkeys(String key) {
        RMap<K, Object> map = redissonClient.getMap(key);
        return map.readAllKeySet();
    }

    /**
     * 获取 Hash 所有值
     */
    @SuppressWarnings("unchecked")
    public <V> List<V> hvals(String key) {
        RMap<Object, V> map = redissonClient.getMap(key);
        return (List<V>) map.readAllValues();
    }

    // ==================== List 操作 ====================

    /**
     * 从左侧推入元素
     */
    public <T> void lpush(String key, T... values) {
        RList<T> list = redissonClient.getList(key);
        for (T value : values) {
            list.add(0, value);
        }
    }

    /**
     * 从右侧推入元素
     */
    public <T> void rpush(String key, T... values) {
        RList<T> list = redissonClient.getList(key);
        for (T value : values) {
            list.add(value);
        }
    }

    /**
     * 从左侧弹出元素
     */
    public <T> T lpop(String key) {
        RList<T> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0);
    }

    /**
     * 从右侧弹出元素
     */
    public <T> T rpop(String key) {
        RList<T> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(list.size() - 1);
    }

    /**
     * 获取 List 长度
     */
    public int llen(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.size();
    }

    /**
     * 获取 List 所有元素
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> lrangeAll(String key) {
        RList<T> list = redissonClient.getList(key);
        return list.readAll();
    }

    // ==================== Set 操作 ====================

    /**
     * 添加元素到 Set
     */
    public <T> void sadd(String key, T... members) {
        RSet<T> set = redissonClient.getSet(key);
        for (T member : members) {
            set.add(member);
        }
    }

    /**
     * 从 Set 移除元素
     */
    public <T> boolean srem(String key, T member) {
        RSet<T> set = redissonClient.getSet(key);
        return set.remove(member);
    }

    /**
     * 判断元素是否在 Set 中
     */
    public <T> boolean sismember(String key, T member) {
        RSet<T> set = redissonClient.getSet(key);
        return set.contains(member);
    }

    /**
     * 获取 Set 所有成员
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> smembers(String key) {
        RSet<T> set = redissonClient.getSet(key);
        return set.readAll();
    }

    /**
     * 获取 Set 大小
     */
    public int scard(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

    // ==================== 分布式锁 ====================

    /**
     * 获取分布式锁（可重入锁）
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 获取公平锁
     */
    public RLock getFairLock(String lockKey) {
        return redissonClient.getFairLock(lockKey);
    }

    /**
     * 获取读写锁
     */
    public RReadWriteLock getReadWriteLock(String lockKey) {
        return redissonClient.getReadWriteLock(lockKey);
    }

    // ==================== 原子操作 ====================

    /**
     * 获取原子长整型
     */
    public RAtomicLong getAtomicLong(String key) {
        return redissonClient.getAtomicLong(key);
    }

    /**
     * 原子递增
     */
    public long increment(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    /**
     * 原子递增指定值
     */
    public long incrementBy(String key, long increment) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(increment);
    }

    /**
     * 原子递减
     */
    public long decrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    // ==================== 高级功能 ====================

    /**
     * 获取布隆过滤器
     */
    public <T> RBloomFilter<T> getBloomFilter(String key) {
        return redissonClient.getBloomFilter(key);
    }

    /**
     * 获取信号量（限流）
     */
    public RSemaphore getSemaphore(String key) {
        return redissonClient.getSemaphore(key);
    }

    /**
     * 获取队列
     */
    public <T> RQueue<T> getQueue(String key) {
        return redissonClient.getQueue(key);
    }

    /**
     * 获取阻塞队列
     */
    public <T> RBlockingQueue<T> getBlockingQueue(String key) {
        return redissonClient.getBlockingQueue(key);
    }
}
