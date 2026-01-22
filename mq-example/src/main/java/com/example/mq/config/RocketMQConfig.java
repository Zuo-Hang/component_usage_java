package com.example.mq.config;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置类
 */
@Configuration
public class RocketMQConfig {

    /**
     * 自定义RocketMQ Template（可选）
     * 如果需要多个不同的Producer实例，可以在这里配置
     */
    @ExtRocketMQTemplateConfiguration(
            group = "example-producer-group",
            nameServer = "${rocketmq.name-server}"
    )
    public static class CustomRocketMQTemplate extends RocketMQTemplate {
    }
}

