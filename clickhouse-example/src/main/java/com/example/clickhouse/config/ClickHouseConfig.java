package com.example.clickhouse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * ClickHouse配置类
 * 
 * 生产环境最佳实践：
 * 1. 使用连接池（HikariCP）
 * 2. 配置合适的连接池大小
 * 3. 设置合理的超时时间
 * 4. 启用批量插入优化
 */
@Slf4j
@Configuration
public class ClickHouseConfig {

    @Value("${clickhouse.url:jdbc:clickhouse://localhost:8123/default}")
    private String jdbcUrl;

    @Value("${clickhouse.username:default}")
    private String username;

    @Value("${clickhouse.password:}")
    private String password;

    /**
     * 配置ClickHouse数据源
     * 
     * 生产环境配置要点：
     * - maxPoolSize: 根据并发查询数设置（通常10-50）
     * - connectionTimeout: 查询可能较慢，设置较长超时（30-60秒）
     * - idleTimeout: ClickHouse连接可以保持较长时间
     */
    @Bean
    public DataSource clickHouseDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // 连接池配置（针对OLAP场景优化）
        config.setMaximumPoolSize(20);  // OLAP查询并发度较低
        config.setMinimumIdle(5);
        config.setConnectionTimeout(60000);  // 60秒（聚合查询可能较慢）
        config.setIdleTimeout(300000);  // 5分钟
        config.setMaxLifetime(1800000);  // 30分钟
        
        // ClickHouse特定配置
        config.addDataSourceProperty("socket_timeout", "60000");
        config.addDataSourceProperty("http_connection_pool", "5");
        config.addDataSourceProperty("http_connection_timeout", "60000");
        
        // 批量插入优化
        config.addDataSourceProperty("insert_quorum", "1");
        config.addDataSourceProperty("insert_quorum_timeout", "60000");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("ClickHouse数据源配置完成: url={}", jdbcUrl);
        return dataSource;
    }

    /**
     * JdbcTemplate Bean
     */
    @Bean
    public JdbcTemplate clickHouseJdbcTemplate(DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }
}
