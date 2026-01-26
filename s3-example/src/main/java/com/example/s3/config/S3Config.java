package com.example.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * S3配置类
 * 支持MinIO和AWS S3
 */
@Configuration
public class S3Config {

    @Value("${s3.endpoint:}")
    private String endpoint;

    @Value("${s3.access-key:}")
    private String accessKey;

    @Value("${s3.secret-key:}")
    private String secretKey;

    @Value("${s3.region:us-east-1}")
    private String region;

    @Value("${s3.bucket:test-bucket}")
    private String defaultBucket;

    /**
     * 创建S3客户端
     * 如果配置了endpoint，则连接到MinIO；否则连接到AWS S3
     */
    @Bean
    public S3Client s3Client() {
        S3Client.Builder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        // 如果配置了endpoint，说明使用的是MinIO或其他S3兼容服务
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
            // MinIO需要禁用路径样式访问检查
            builder.forcePathStyle(true);
        }

        return builder.build();
    }

    /**
     * 获取默认Bucket名称
     */
    public String getDefaultBucket() {
        return defaultBucket;
    }
}
