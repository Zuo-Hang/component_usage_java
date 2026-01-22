package com.example.mysql;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MySQL示例应用主类
 */
@SpringBootApplication
@MapperScan("com.example.mysql.mapper")
public class MySQLExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySQLExampleApplication.class, args);
    }
}

