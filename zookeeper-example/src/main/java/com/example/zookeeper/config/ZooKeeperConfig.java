package com.example.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ZooKeeper配置类
 */
@Configuration
public class ZooKeeperConfig {

    @Value("${zookeeper.connect-string:localhost:2181}")
    private String connectString;

    @Value("${zookeeper.session-timeout:30000}")
    private int sessionTimeout;

    @Value("${zookeeper.connection-timeout:15000}")
    private int connectionTimeout;

    @Value("${zookeeper.retry-times:3}")
    private int retryTimes;

    @Value("${zookeeper.retry-interval:1000}")
    private int retryInterval;

    /**
     * 创建CuratorFramework客户端
     */
    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(new RetryNTimes(retryTimes, retryInterval))
                .build();
        
        client.start();
        return client;
    }
}
