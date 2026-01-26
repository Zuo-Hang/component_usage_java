package com.example.doris.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Doris配置类
 * 
 * 生产环境最佳实践：
 * 1. 使用MySQL JDBC驱动（Doris兼容MySQL协议）
 * 2. 配置连接池（HikariCP）
 * 3. 针对高并发场景优化连接池
 * 4. 设置合理的超时时间
 */
@Slf4j
@Configuration
public class DorisConfig {

    @Value("${doris.url:jdbc:mysql://localhost:9030/demo}")
    private String jdbcUrl;

    @Value("${doris.username:root}")
    private String username;

    @Value("${doris.password:}")
    private String password;

    /**
     * 配置Doris数据源
     * 
     * 生产环境配置要点：
     * - maxPoolSize: Doris支持高并发，可以设置较大（50-100）
     * - connectionTimeout: 复杂JOIN查询可能较慢，设置较长超时（30-60秒）
     * - 使用MySQL协议，兼容MySQL客户端工具
     */
    @Bean
    public DataSource dorisDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置（针对高并发OLAP场景优化）
        config.setMaximumPoolSize(50);  // Doris支持高并发，设置较大
        config.setMinimumIdle(10);
        config.setConnectionTimeout(60000);  // 60秒（复杂JOIN查询可能较慢）
        config.setIdleTimeout(300000);  // 5分钟
        config.setMaxLifetime(1800000);  // 30分钟
        
        // MySQL协议特定配置
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Doris特定配置
        config.addDataSourceProperty("rewriteBatchedStatements", "true");  // 批量插入优化
        
        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("Doris数据源配置完成: url={}", jdbcUrl);
        return dataSource;
    }

    /**
     * JdbcTemplate Bean
     */
    @Bean
    public JdbcTemplate dorisJdbcTemplate(DataSource dorisDataSource) {
        return new JdbcTemplate(dorisDataSource);
    }
}
