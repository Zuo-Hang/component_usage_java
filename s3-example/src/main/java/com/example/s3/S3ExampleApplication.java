package com.example.s3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * S3示例应用主类
 * 支持MinIO和AWS S3
 */
@SpringBootApplication
public class S3ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(S3ExampleApplication.class, args);
    }
}
