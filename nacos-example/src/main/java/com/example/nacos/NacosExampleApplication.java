package com.example.nacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Nacos示例应用主类
 * 
 * @EnableDiscoveryClient: 启用服务发现客户端
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosExampleApplication.class, args);
    }
}
