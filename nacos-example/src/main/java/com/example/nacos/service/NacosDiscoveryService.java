package com.example.nacos.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos服务发现服务
 * 演示如何发现和调用其他服务
 */
@Slf4j
@Service
public class NacosDiscoveryService {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired(required = false)
    private NamingService namingService;

    /**
     * 获取所有已注册的服务名称
     */
    public List<String> getAllServices() {
        List<String> services = discoveryClient.getServices();
        log.info("发现的服务列表: {}", services);
        return services;
    }

    /**
     * 根据服务名称获取服务实例列表
     * 
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    public List<ServiceInstance> getServiceInstances(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        log.info("服务 {} 的实例列表: {}", serviceName, instances);
        return instances;
    }

    /**
     * 获取服务的第一个可用实例
     * 
     * @param serviceName 服务名称
     * @return 服务实例，如果不存在返回null
     */
    public ServiceInstance getFirstInstance(String serviceName) {
        List<ServiceInstance> instances = getServiceInstances(serviceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0);
        }
        return null;
    }

    /**
     * 构建服务调用URL
     * 
     * @param serviceName 服务名称
     * @param path 请求路径
     * @return 完整的URL
     */
    public String buildServiceUrl(String serviceName, String path) {
        ServiceInstance instance = getFirstInstance(serviceName);
        if (instance != null) {
            String url = String.format("http://%s:%d%s", 
                    instance.getHost(), instance.getPort(), path);
            log.info("构建服务URL: {}", url);
            return url;
        }
        throw new RuntimeException("服务 " + serviceName + " 不可用");
    }

    /**
     * 使用Nacos原生API获取服务实例（健康检查）
     * 
     * @param serviceName 服务名称
     * @return 健康实例列表
     */
    public List<Instance> getHealthyInstances(String serviceName) {
        if (namingService == null) {
            log.warn("NamingService未注入，无法使用原生API");
            return List.of();
        }
        
        try {
            List<Instance> instances = namingService.selectInstances(serviceName, true);
            log.info("服务 {} 的健康实例: {}", serviceName, instances);
            return instances;
        } catch (NacosException e) {
            log.error("获取服务实例失败: {}", serviceName, e);
            return List.of();
        }
    }

    /**
     * 订阅服务变化（监听服务上下线）
     * 
     * @param serviceName 服务名称
     */
    public void subscribeService(String serviceName) {
        if (namingService == null) {
            log.warn("NamingService未注入，无法订阅服务");
            return;
        }
        
        try {
            namingService.subscribe(serviceName, instances -> {
                log.info("服务 {} 实例变化: {}", serviceName, instances);
            });
            log.info("已订阅服务: {}", serviceName);
        } catch (NacosException e) {
            log.error("订阅服务失败: {}", serviceName, e);
        }
    }
}
