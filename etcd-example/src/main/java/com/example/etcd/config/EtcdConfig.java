package com.example.etcd.config;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Watch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Etcd配置类
 * 
 * 生产环境最佳实践：
 * 1. 使用连接池（Client自动管理）
 * 2. 配置超时时间
 * 3. 使用租约（Lease）管理服务注册
 * 4. 监听键变化（Watch）
 */
@Slf4j
@Configuration
public class EtcdConfig {

    @Value("${etcd.endpoints:http://localhost:2379}")
    private String endpoints;

    /**
     * 创建Etcd客户端
     * 
     * 支持多节点配置，格式：http://host1:2379,http://host2:2379
     */
    @Bean(destroyMethod = "close")
    public Client etcdClient() {
        String[] endpointArray = endpoints.split(",");
        List<URI> uris = new ArrayList<>();
        
        for (String endpoint : endpointArray) {
            uris.add(URI.create(endpoint.trim()));
        }
        
        Client client = Client.builder()
                .endpoints(uris.toArray(new URI[0]))
                // 连接超时（毫秒）
                .connectTimeout(5000)
                // 请求超时（毫秒）
                .timeout(30000)
                .build();
        
        log.info("Etcd客户端创建成功: endpoints={}", endpoints);
        return client;
    }

    /**
     * KV客户端（键值操作）
     */
    @Bean
    public KV kvClient(Client etcdClient) {
        return etcdClient.getKVClient();
    }

    /**
     * Lease客户端（租约操作）
     */
    @Bean
    public Lease leaseClient(Client etcdClient) {
        return etcdClient.getLeaseClient();
    }

    /**
     * Watch客户端（监听操作）
     */
    @Bean
    public Watch watchClient(Client etcdClient) {
        return etcdClient.getWatchClient();
    }
}
