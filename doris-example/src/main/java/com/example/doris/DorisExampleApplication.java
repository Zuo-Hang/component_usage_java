package com.example.doris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Doris示例应用主类
 * 
 * 注意：Doris是OLAP数据库，主要用于数据分析场景
 * 
 * Doris vs ClickHouse 核心差异：
 * 1. MySQL协议兼容：使用标准MySQL JDBC驱动
 * 2. 实时更新：支持UPDATE/DELETE操作
 * 3. 多表JOIN：CBO优化，复杂JOIN性能更好
 * 4. 高并发：支持更高的并发查询
 * 5. 物化视图：支持多表物化视图和查询重写
 */
@SpringBootApplication
public class DorisExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DorisExampleApplication.class, args);
    }
}
