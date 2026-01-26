package com.example.etcd.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Etcd键值存储服务
 * 
 * 核心功能：
 * 1. PUT - 写入键值对
 * 2. GET - 读取键值对
 * 3. DELETE - 删除键值对
 * 4. 前缀查询
 * 5. 批量操作
 */
@Slf4j
@Service
public class EtcdKeyValueService {

    @Autowired
    private KV kvClient;

    /**
     * 写入键值对
     * 
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public boolean put(String key, String value) {
        try {
            ByteSequence keyBytes = ByteSequence.from(key, StandardCharsets.UTF_8);
            ByteSequence valueBytes = ByteSequence.from(value, StandardCharsets.UTF_8);
            
            kvClient.put(keyBytes, valueBytes).get();
            log.info("写入键值对成功: key={}, value={}", key, value);
            return true;
        } catch (Exception e) {
            log.error("写入键值对失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 写入键值对（带租约）
     * 
     * @param key 键
     * @param value 值
     * @param leaseId 租约ID
     * @return 是否成功
     */
    public boolean putWithLease(String key, String value, long leaseId) {
        try {
            ByteSequence keyBytes = ByteSequence.from(key, StandardCharsets.UTF_8);
            ByteSequence valueBytes = ByteSequence.from(value, StandardCharsets.UTF_8);
            
            PutOption putOption = PutOption.newBuilder()
                    .withLeaseId(leaseId)
                    .build();
            
            kvClient.put(keyBytes, valueBytes, putOption).get();
            log.info("写入键值对（带租约）成功: key={}, value={}, leaseId={}", key, value, leaseId);
            return true;
        } catch (Exception e) {
            log.error("写入键值对（带租约）失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 读取键值对
     * 
     * @param key 键
     * @return 值，如果不存在返回null
     */
    public String get(String key) {
        try {
            ByteSequence keyBytes = ByteSequence.from(key, StandardCharsets.UTF_8);
            List<KeyValue> keyValues = kvClient.get(keyBytes).get().getKvs();
            
            if (keyValues.isEmpty()) {
                log.info("键不存在: key={}", key);
                return null;
            }
            
            String value = keyValues.get(0).getValue().toString(StandardCharsets.UTF_8);
            log.info("读取键值对成功: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("读取键值对失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 读取键值对（带版本信息）
     * 
     * @param key 键
     * @return 键值对信息
     */
    public KeyValue getWithMetadata(String key) {
        try {
            ByteSequence keyBytes = ByteSequence.from(key, StandardCharsets.UTF_8);
            List<KeyValue> keyValues = kvClient.get(keyBytes).get().getKvs();
            
            if (keyValues.isEmpty()) {
                return null;
            }
            
            return keyValues.get(0);
        } catch (Exception e) {
            log.error("读取键值对（带版本信息）失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 前缀查询（获取所有以prefix开头的键值对）
     * 
     * @param prefix 前缀
     * @return 键值对列表
     */
    public List<KeyValue> getByPrefix(String prefix) {
        try {
            ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);
            GetOption getOption = GetOption.newBuilder()
                    .withPrefix(prefixBytes)
                    .build();
            
            List<KeyValue> keyValues = kvClient.get(prefixBytes, getOption).get().getKvs();
            log.info("前缀查询成功: prefix={}, count={}", prefix, keyValues.size());
            return keyValues;
        } catch (Exception e) {
            log.error("前缀查询失败: prefix={}", prefix, e);
            return List.of();
        }
    }

    /**
     * 获取所有键值对（转换为Map）
     * 
     * @param prefix 前缀
     * @return 键值对Map
     */
    public java.util.Map<String, String> getAllByPrefix(String prefix) {
        List<KeyValue> keyValues = getByPrefix(prefix);
        return keyValues.stream()
                .collect(Collectors.toMap(
                        kv -> kv.getKey().toString(StandardCharsets.UTF_8),
                        kv -> kv.getValue().toString(StandardCharsets.UTF_8)
                ));
    }

    /**
     * 删除键值对
     * 
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        try {
            ByteSequence keyBytes = ByteSequence.from(key, StandardCharsets.UTF_8);
            kvClient.delete(keyBytes).get();
            log.info("删除键值对成功: key={}", key);
            return true;
        } catch (Exception e) {
            log.error("删除键值对失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 删除所有以prefix开头的键值对
     * 
     * @param prefix 前缀
     * @return 删除的数量
     */
    public long deleteByPrefix(String prefix) {
        try {
            ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);
            GetOption getOption = GetOption.newBuilder()
                    .withPrefix(prefixBytes)
                    .build();
            
            // 先获取所有键
            List<KeyValue> keyValues = kvClient.get(prefixBytes, getOption).get().getKvs();
            
            // 批量删除
            long deletedCount = 0;
            for (KeyValue kv : keyValues) {
                if (delete(kv.getKey().toString(StandardCharsets.UTF_8))) {
                    deletedCount++;
                }
            }
            
            log.info("前缀删除成功: prefix={}, deletedCount={}", prefix, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("前缀删除失败: prefix={}", prefix, e);
            return 0;
        }
    }

    /**
     * 检查键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        String value = get(key);
        return value != null;
    }

    /**
     * 批量写入键值对
     * 
     * @param keyValues 键值对Map
     * @return 成功写入的数量
     */
    public int batchPut(java.util.Map<String, String> keyValues) {
        int successCount = 0;
        for (java.util.Map.Entry<String, String> entry : keyValues.entrySet()) {
            if (put(entry.getKey(), entry.getValue())) {
                successCount++;
            }
        }
        log.info("批量写入完成: total={}, success={}", keyValues.size(), successCount);
        return successCount;
    }
}
