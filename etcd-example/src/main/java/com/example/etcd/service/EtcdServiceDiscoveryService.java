package com.example.etcd.service;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Etcd服务发现服务
 * 
 * 核心功能：
 * 1. 服务注册（带租约，自动续约）
 * 2. 服务发现（查询服务实例）
 * 3. 服务监听（监听服务上下线）
 * 4. 服务注销
 * 
 * 服务注册格式：/services/{serviceName}/{instanceId}
 * 例如：/services/user-service/instance-1
 */
@Slf4j
@Service
public class EtcdServiceDiscoveryService {

    @Autowired
    private KV kvClient;

    @Autowired
    private Lease leaseClient;

    @Autowired
    private Watch watchClient;

    @Value("${etcd.service.prefix:/services}")
    private String servicePrefix;

    /**
     * 服务注册
     * 
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     * @param instanceInfo 实例信息（JSON格式，包含host、port等）
     * @param ttlSeconds 租约TTL（秒），服务会定期续约
     * @return 租约ID，用于后续续约和注销
     */
    public long registerService(String serviceName, String instanceId, String instanceInfo, long ttlSeconds) {
        try {
            // 1. 创建租约
            long leaseId = leaseClient.grant(ttlSeconds).get().getID();
            log.info("创建租约成功: leaseId={}, ttl={}s", leaseId, ttlSeconds);

            // 2. 注册服务（带租约）
            String serviceKey = String.format("%s/%s/%s", servicePrefix, serviceName, instanceId);
            ByteSequence keyBytes = ByteSequence.from(serviceKey, StandardCharsets.UTF_8);
            ByteSequence valueBytes = ByteSequence.from(instanceInfo, StandardCharsets.UTF_8);
            
            PutOption putOption = PutOption.newBuilder()
                    .withLeaseId(leaseId)
                    .build();
            
            kvClient.put(keyBytes, valueBytes, putOption).get();
            log.info("服务注册成功: serviceName={}, instanceId={}, leaseId={}", 
                    serviceName, instanceId, leaseId);

            // 3. 启动自动续约（保持服务在线）
            startLeaseKeepAlive(leaseId);
            
            return leaseId;
        } catch (Exception e) {
            log.error("服务注册失败: serviceName={}, instanceId={}", serviceName, instanceId, e);
            throw new RuntimeException("服务注册失败", e);
        }
    }

    /**
     * 启动租约自动续约
     * 
     * @param leaseId 租约ID
     */
    private void startLeaseKeepAlive(long leaseId) {
        leaseClient.keepAlive(leaseId, new io.etcd.jetcd.lease.LeaseResponseObserver() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                log.debug("租约续约成功: leaseId={}, ttl={}", leaseId, response.getTTL());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("租约续约失败: leaseId={}", leaseId, throwable);
            }

            @Override
            public void onCompleted() {
                log.warn("租约续约完成: leaseId={}", leaseId);
            }
        });
    }

    /**
     * 服务注销
     * 
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     * @return 是否成功
     */
    public boolean unregisterService(String serviceName, String instanceId) {
        try {
            String serviceKey = String.format("%s/%s/%s", servicePrefix, serviceName, instanceId);
            ByteSequence keyBytes = ByteSequence.from(serviceKey, StandardCharsets.UTF_8);
            
            kvClient.delete(keyBytes).get();
            log.info("服务注销成功: serviceName={}, instanceId={}", serviceName, instanceId);
            return true;
        } catch (Exception e) {
            log.error("服务注销失败: serviceName={}, instanceId={}", serviceName, instanceId, e);
            return false;
        }
    }

    /**
     * 发现服务实例列表
     * 
     * @param serviceName 服务名称
     * @return 服务实例列表（JSON格式）
     */
    public List<String> discoverService(String serviceName) {
        try {
            String prefix = String.format("%s/%s/", servicePrefix, serviceName);
            ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);
            
            io.etcd.jetcd.options.GetOption getOption = io.etcd.jetcd.options.GetOption.newBuilder()
                    .withPrefix(prefixBytes)
                    .build();
            
            List<io.etcd.jetcd.KeyValue> keyValues = kvClient.get(prefixBytes, getOption).get().getKvs();
            
            List<String> instances = new ArrayList<>();
            for (io.etcd.jetcd.KeyValue kv : keyValues) {
                String instanceInfo = kv.getValue().toString(StandardCharsets.UTF_8);
                instances.add(instanceInfo);
            }
            
            log.info("发现服务实例: serviceName={}, count={}", serviceName, instances.size());
            return instances;
        } catch (Exception e) {
            log.error("发现服务失败: serviceName={}", serviceName, e);
            return List.of();
        }
    }

    /**
     * 获取所有服务名称
     * 
     * @return 服务名称列表
     */
    public List<String> getAllServices() {
        try {
            ByteSequence prefixBytes = ByteSequence.from(servicePrefix, StandardCharsets.UTF_8);
            io.etcd.jetcd.options.GetOption getOption = io.etcd.jetcd.options.GetOption.newBuilder()
                    .withPrefix(prefixBytes)
                    .withKeysOnly(true)
                    .build();
            
            List<io.etcd.jetcd.KeyValue> keyValues = kvClient.get(prefixBytes, getOption).get().getKvs();
            
            List<String> services = new ArrayList<>();
            for (io.etcd.jetcd.KeyValue kv : keyValues) {
                String key = kv.getKey().toString(StandardCharsets.UTF_8);
                // 解析服务名称：/services/{serviceName}/{instanceId}
                String[] parts = key.split("/");
                if (parts.length >= 3) {
                    String serviceName = parts[2];
                    if (!services.contains(serviceName)) {
                        services.add(serviceName);
                    }
                }
            }
            
            log.info("获取所有服务: count={}", services.size());
            return services;
        } catch (Exception e) {
            log.error("获取所有服务失败", e);
            return List.of();
        }
    }

    /**
     * 监听服务变化（服务上下线）
     * 
     * @param serviceName 服务名称
     * @param onServiceChange 服务变化回调
     */
    public void watchService(String serviceName, Consumer<List<String>> onServiceChange) {
        String prefix = String.format("%s/%s/", servicePrefix, serviceName);
        ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);
        
        watchClient.watch(prefixBytes, watchResponse -> {
            for (WatchEvent event : watchResponse.getEvents()) {
                log.info("服务变化事件: type={}, key={}", 
                        event.getEventType(), 
                        event.getKeyValue().getKey().toString(StandardCharsets.UTF_8));
                
                // 重新获取服务实例列表
                List<String> instances = discoverService(serviceName);
                onServiceChange.accept(instances);
            }
        });
        
        log.info("开始监听服务: serviceName={}", serviceName);
    }

    /**
     * 获取服务的第一个可用实例
     * 
     * @param serviceName 服务名称
     * @return 实例信息（JSON格式），如果不存在返回null
     */
    public String getFirstInstance(String serviceName) {
        List<String> instances = discoverService(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0);
        }
        return null;
    }
}
