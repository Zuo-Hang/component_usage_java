package com.example.etcd.service;

import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Etcd租约服务
 * 
 * 核心功能：
 * 1. 创建租约（带TTL）
 * 2. 续约（KeepAlive）
 * 3. 撤销租约
 * 4. 查询租约信息
 * 
 * 租约用途：
 * - 服务注册（服务下线时自动删除）
 * - 临时数据（自动过期）
 * - 分布式锁（锁超时自动释放）
 */
@Slf4j
@Service
public class EtcdLeaseService {

    @Autowired
    private Lease leaseClient;

    /**
     * 创建租约
     * 
     * @param ttlSeconds TTL（秒）
     * @return 租约ID
     */
    public long grantLease(long ttlSeconds) {
        try {
            long leaseId = leaseClient.grant(ttlSeconds).get().getID();
            log.info("创建租约成功: leaseId={}, ttl={}s", leaseId, ttlSeconds);
            return leaseId;
        } catch (Exception e) {
            log.error("创建租约失败: ttl={}s", ttlSeconds, e);
            throw new RuntimeException("创建租约失败", e);
        }
    }

    /**
     * 续约（KeepAlive）
     * 
     * @param leaseId 租约ID
     * @return 续约后的TTL（秒）
     */
    public long keepAlive(long leaseId) {
        try {
            LeaseKeepAliveResponse response = leaseClient.keepAliveOnce(leaseId).get();
            long ttl = response.getTTL();
            log.info("续约成功: leaseId={}, ttl={}s", leaseId, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("续约失败: leaseId={}", leaseId, e);
            throw new RuntimeException("续约失败", e);
        }
    }

    /**
     * 撤销租约
     * 
     * @param leaseId 租约ID
     * @return 是否成功
     */
    public boolean revokeLease(long leaseId) {
        try {
            leaseClient.revoke(leaseId).get();
            log.info("撤销租约成功: leaseId={}", leaseId);
            return true;
        } catch (Exception e) {
            log.error("撤销租约失败: leaseId={}", leaseId, e);
            return false;
        }
    }

    /**
     * 查询租约信息
     * 
     * @param leaseId 租约ID
     * @return TTL（秒），如果租约不存在返回-1
     */
    public long getLeaseTTL(long leaseId) {
        try {
            CompletableFuture<io.etcd.jetcd.lease.LeaseTimeToLiveResponse> future = 
                    leaseClient.timeToLive(leaseId, false);
            io.etcd.jetcd.lease.LeaseTimeToLiveResponse response = future.get();
            long ttl = response.getTTL();
            log.info("查询租约信息: leaseId={}, ttl={}s", leaseId, ttl);
            return ttl;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof io.grpc.StatusRuntimeException) {
                io.grpc.StatusRuntimeException statusException = 
                        (io.grpc.StatusRuntimeException) e.getCause();
                if (statusException.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                    log.warn("租约不存在: leaseId={}", leaseId);
                    return -1;
                }
            }
            log.error("查询租约信息失败: leaseId={}", leaseId, e);
            return -1;
        } catch (Exception e) {
            log.error("查询租约信息失败: leaseId={}", leaseId, e);
            return -1;
        }
    }

    /**
     * 启动自动续约（持续续约，直到租约被撤销）
     * 
     * @param leaseId 租约ID
     */
    public void startAutoKeepAlive(long leaseId) {
        leaseClient.keepAlive(leaseId, new io.etcd.jetcd.lease.LeaseResponseObserver() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                log.debug("自动续约成功: leaseId={}, ttl={}s", leaseId, response.getTTL());
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("自动续约失败: leaseId={}", leaseId, throwable);
            }

            @Override
            public void onCompleted() {
                log.warn("自动续约完成: leaseId={}", leaseId);
            }
        });
        log.info("启动自动续约: leaseId={}", leaseId);
    }
}
