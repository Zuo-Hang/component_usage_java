package com.example.clickhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClickHouse示例应用主类
 * 
 * 注意：ClickHouse是OLAP数据库，主要用于数据分析场景
 * - 适合：批量数据导入、聚合分析、时间序列查询
 * - 不适合：高频事务、单条记录频繁更新
 */
@SpringBootApplication
public class ClickHouseExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickHouseExampleApplication.class, args);
    }
}
