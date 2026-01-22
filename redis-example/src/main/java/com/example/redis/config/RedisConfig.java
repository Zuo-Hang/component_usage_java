package com.example.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis配置类
 * 同时配置Redisson和Jedis连接池
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int database;

    /**
     * 配置RedissonClient
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单节点模式配置
        String address = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database);
        
        // 如果有密码，设置密码
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        // 连接池配置
        config.useSingleServer()
                .setConnectionPoolSize(10)        // 连接池大小
                .setConnectionMinimumIdleSize(5)  // 最小空闲连接数
                .setConnectTimeout(3000)          // 连接超时时间（毫秒）
                .setTimeout(3000)                 // 命令执行超时时间（毫秒）
                .setRetryAttempts(3)              // 重试次数
                .setRetryInterval(1500);          // 重试间隔（毫秒）
        
        return Redisson.create(config);
    }

    /**
     * 配置Jedis连接池
     */
    @Bean(destroyMethod = "close")
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);              // 最大连接数
        poolConfig.setMaxIdle(8);                // 最大空闲连接数
        poolConfig.setMinIdle(2);                // 最小空闲连接数
        poolConfig.setTestOnBorrow(true);        // 获取连接时测试
        poolConfig.setTestOnReturn(true);        // 归还连接时测试
        poolConfig.setTestWhileIdle(true);       // 空闲时测试
        poolConfig.setMaxWait(java.time.Duration.ofMillis(3000));  // 最大等待时间

        if (redisPassword != null && !redisPassword.isEmpty()) {
            return new JedisPool(poolConfig, redisHost, redisPort, 3000, redisPassword, database);
        } else {
            return new JedisPool(poolConfig, redisHost, redisPort, 3000, null, database);
        }
    }
}

