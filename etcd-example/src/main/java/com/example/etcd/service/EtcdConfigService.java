package com.example.etcd.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Etcd配置管理服务
 * 
 * 核心功能：
 * 1. 配置写入
 * 2. 配置读取
 * 3. 配置监听（配置变更通知）
 * 4. 批量配置管理
 * 
 * 配置存储格式：/config/{application}/{key}
 * 例如：/config/myapp/database.url
 */
@Slf4j
@Service
public class EtcdConfigService {

    @Autowired
    private KV kvClient;

    @Autowired
    private Watch watchClient;

    @Autowired
    private EtcdKeyValueService keyValueService;

    @Value("${etcd.config.prefix:/config}")
    private String configPrefix;

    /**
     * 写入配置
     * 
     * @param application 应用名称
     * @param key 配置键
     * @param value 配置值
     * @return 是否成功
     */
    public boolean putConfig(String application, String key, String value) {
        String configKey = String.format("%s/%s/%s", configPrefix, application, key);
        return keyValueService.put(configKey, value);
    }

    /**
     * 读取配置
     * 
     * @param application 应用名称
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    public String getConfig(String application, String key) {
        String configKey = String.format("%s/%s/%s", configPrefix, application, key);
        return keyValueService.get(configKey);
    }

    /**
     * 删除配置
     * 
     * @param application 应用名称
     * @param key 配置键
     * @return 是否成功
     */
    public boolean deleteConfig(String application, String key) {
        String configKey = String.format("%s/%s/%s", configPrefix, application, key);
        return keyValueService.delete(configKey);
    }

    /**
     * 获取应用的所有配置
     * 
     * @param application 应用名称
     * @return 配置Map
     */
    public Map<String, String> getAllConfigs(String application) {
        String prefix = String.format("%s/%s/", configPrefix, application);
        return keyValueService.getAllByPrefix(prefix);
    }

    /**
     * 批量写入配置
     * 
     * @param application 应用名称
     * @param configs 配置Map
     * @return 成功写入的数量
     */
    public int batchPutConfigs(String application, Map<String, String> configs) {
        Map<String, String> fullConfigs = new HashMap<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configKey = String.format("%s/%s/%s", configPrefix, application, entry.getKey());
            fullConfigs.put(configKey, entry.getValue());
        }
        return keyValueService.batchPut(fullConfigs);
    }

    /**
     * 监听配置变化
     * 
     * @param application 应用名称
     * @param onConfigChange 配置变化回调
     */
    public void watchConfig(String application, Consumer<Map<String, String>> onConfigChange) {
        String prefix = String.format("%s/%s/", configPrefix, application);
        ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);
        
        watchClient.watch(prefixBytes, watchResponse -> {
            for (WatchEvent event : watchResponse.getEvents()) {
                String key = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                log.info("配置变化事件: type={}, key={}", event.getEventType(), key);
                
                // 重新获取所有配置
                Map<String, String> configs = getAllConfigs(application);
                onConfigChange.accept(configs);
            }
        });
        
        log.info("开始监听配置: application={}", application);
    }

    /**
     * 监听单个配置键的变化
     * 
     * @param application 应用名称
     * @param key 配置键
     * @param onConfigChange 配置变化回调
     */
    public void watchConfigKey(String application, String key, Consumer<String> onConfigChange) {
        String configKey = String.format("%s/%s/%s", configPrefix, application, key);
        ByteSequence keyBytes = ByteSequence.from(configKey, StandardCharsets.UTF_8);
        
        watchClient.watch(keyBytes, watchResponse -> {
            for (WatchEvent event : watchResponse.getEvents()) {
                String value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                log.info("配置键变化事件: type={}, key={}, value={}", 
                        event.getEventType(), key, value);
                onConfigChange.accept(value);
            }
        });
        
        log.info("开始监听配置键: application={}, key={}", application, key);
    }
}
