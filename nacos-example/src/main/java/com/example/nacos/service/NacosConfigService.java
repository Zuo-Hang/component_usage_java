package com.example.nacos.service;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * Nacos配置管理服务
 * 演示如何读取和监听配置变化
 */
@Slf4j
@Service
@RefreshScope // 支持配置动态刷新
public class NacosConfigService {

    @Autowired(required = false)
    private ConfigService configService;

    // 从Nacos配置中心读取的配置（通过@Value注入）
    @Value("${app.name:nacos-example}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:}")
    private String appDescription;

    /**
     * 获取应用配置信息
     */
    public AppConfig getAppConfig() {
        AppConfig config = new AppConfig();
        config.setName(appName);
        config.setVersion(appVersion);
        config.setDescription(appDescription);
        return config;
    }

    /**
     * 使用Nacos原生API获取配置
     * 
     * @param dataId 配置ID
     * @param group 配置组（默认DEFAULT_GROUP）
     * @return 配置内容
     */
    public String getConfig(String dataId, String group) {
        if (configService == null) {
            log.warn("ConfigService未注入，无法获取配置");
            return null;
        }
        
        try {
            String content = configService.getConfig(dataId, group, 5000);
            log.info("获取配置: dataId={}, group={}, content={}", dataId, group, content);
            return content;
        } catch (NacosException e) {
            log.error("获取配置失败: dataId={}, group={}", dataId, group, e);
            return null;
        }
    }

    /**
     * 发布配置到Nacos
     * 
     * @param dataId 配置ID
     * @param group 配置组
     * @param content 配置内容
     * @return 是否成功
     */
    public boolean publishConfig(String dataId, String group, String content) {
        if (configService == null) {
            log.warn("ConfigService未注入，无法发布配置");
            return false;
        }
        
        try {
            boolean result = configService.publishConfig(dataId, group, content);
            log.info("发布配置: dataId={}, group={}, result={}", dataId, group, result);
            return result;
        } catch (NacosException e) {
            log.error("发布配置失败: dataId={}, group={}", dataId, group, e);
            return false;
        }
    }

    /**
     * 删除配置
     * 
     * @param dataId 配置ID
     * @param group 配置组
     * @return 是否成功
     */
    public boolean removeConfig(String dataId, String group) {
        if (configService == null) {
            log.warn("ConfigService未注入，无法删除配置");
            return false;
        }
        
        try {
            boolean result = configService.removeConfig(dataId, group);
            log.info("删除配置: dataId={}, group={}, result={}", dataId, group, result);
            return result;
        } catch (NacosException e) {
            log.error("删除配置失败: dataId={}, group={}", dataId, group, e);
            return false;
        }
    }

    /**
     * 监听配置变化
     * 
     * @param dataId 配置ID
     * @param group 配置组
     */
    @PostConstruct
    public void listenConfig() {
        if (configService == null) {
            return;
        }
        
        // 监听应用配置
        String dataId = "nacos-example.properties";
        String group = "DEFAULT_GROUP";
        
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("配置发生变化: dataId={}, group={}, newConfig={}", 
                            dataId, group, configInfo);
                    // 这里可以触发配置刷新逻辑
                }

                @Override
                public Executor getExecutor() {
                    return null; // 使用默认执行器
                }
            });
            log.info("已监听配置变化: dataId={}, group={}", dataId, group);
        } catch (NacosException e) {
            log.error("监听配置失败: dataId={}, group={}", dataId, group, e);
        }
    }

    /**
     * 应用配置DTO
     */
    public static class AppConfig {
        private String name;
        private String version;
        private String description;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
