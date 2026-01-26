package com.example.etcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Etcd示例应用主类
 * 
 * Etcd核心功能：
 * 1. 分布式键值存储（PUT/GET/DELETE）
 * 2. 服务发现（注册/发现/监听）
 * 3. 配置管理（配置读写/监听）
 * 4. 租约（Lease）和TTL（自动过期）
 * 
 * Etcd vs ZooKeeper：
 * - Etcd使用HTTP/gRPC协议，ZooKeeper使用TCP协议
 * - Etcd支持租约和TTL，ZooKeeper使用临时节点
 * - Etcd使用Raft一致性算法，ZooKeeper使用ZAB协议
 */
@SpringBootApplication
public class EtcdExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(EtcdExampleApplication.class, args);
    }
}
