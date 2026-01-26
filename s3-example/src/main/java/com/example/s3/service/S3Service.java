package com.example.s3.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * S3服务类
 * 提供文件上传、下载、删除、列表等功能
 */
@Slf4j
@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${s3.bucket:test-bucket}")
    private String defaultBucket;

    /**
     * 创建Bucket
     * 
     * @param bucketName Bucket名称
     * @return 是否成功
     */
    public boolean createBucket(String bucketName) {
        try {
            CreateBucketRequest request = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(request);
            log.info("创建Bucket成功: {}", bucketName);
            return true;
        } catch (BucketAlreadyExistsException e) {
            log.warn("Bucket已存在: {}", bucketName);
            return false;
        } catch (Exception e) {
            log.error("创建Bucket失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 删除Bucket
     * 
     * @param bucketName Bucket名称
     * @return 是否成功
     */
    public boolean deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest request = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.deleteBucket(request);
            log.info("删除Bucket成功: {}", bucketName);
            return true;
        } catch (Exception e) {
            log.error("删除Bucket失败: {}", bucketName, e);
            return false;
        }
    }

    /**
     * 列出所有Bucket
     * 
     * @return Bucket名称列表
     */
    public List<String> listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<String> buckets = response.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
            log.info("列出Bucket列表: {}", buckets);
            return buckets;
        } catch (Exception e) {
            log.error("列出Bucket失败", e);
            return List.of();
        }
    }

    /**
     * 上传文件
     * 
     * @param bucketName Bucket名称
     * @param objectKey 对象键（文件路径）
     * @param inputStream 文件输入流
     * @param contentType 内容类型
     * @return 是否成功
     */
    public boolean uploadFile(String bucketName, String objectKey, InputStream inputStream, String contentType) {
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromByteBuffer(ByteBuffer.wrap(bytes)));
            log.info("上传文件成功: bucket={}, key={}", bucketName, objectKey);
            return true;
        } catch (Exception e) {
            log.error("上传文件失败: bucket={}, key={}", bucketName, objectKey, e);
            return false;
        }
    }

    /**
     * 上传文件（使用默认Bucket）
     * 
     * @param objectKey 对象键
     * @param inputStream 文件输入流
     * @param contentType 内容类型
     * @return 是否成功
     */
    public boolean uploadFile(String objectKey, InputStream inputStream, String contentType) {
        return uploadFile(defaultBucket, objectKey, inputStream, contentType);
    }

    /**
     * 下载文件
     * 
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 文件字节数组
     */
    public byte[] downloadFile(String bucketName, String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.copy(response, outputStream);
            byte[] bytes = outputStream.toByteArray();
            log.info("下载文件成功: bucket={}, key={}, size={}", bucketName, objectKey, bytes.length);
            return bytes;
        } catch (NoSuchKeyException e) {
            log.warn("文件不存在: bucket={}, key={}", bucketName, objectKey);
            return null;
        } catch (Exception e) {
            log.error("下载文件失败: bucket={}, key={}", bucketName, objectKey, e);
            return null;
        }
    }

    /**
     * 下载文件（使用默认Bucket）
     * 
     * @param objectKey 对象键
     * @return 文件字节数组
     */
    public byte[] downloadFile(String objectKey) {
        return downloadFile(defaultBucket, objectKey);
    }

    /**
     * 删除文件
     * 
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 是否成功
     */
    public boolean deleteFile(String bucketName, String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("删除文件成功: bucket={}, key={}", bucketName, objectKey);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败: bucket={}, key={}", bucketName, objectKey, e);
            return false;
        }
    }

    /**
     * 删除文件（使用默认Bucket）
     * 
     * @param objectKey 对象键
     * @return 是否成功
     */
    public boolean deleteFile(String objectKey) {
        return deleteFile(defaultBucket, objectKey);
    }

    /**
     * 列出Bucket中的所有对象
     * 
     * @param bucketName Bucket名称
     * @param prefix 前缀过滤（可选）
     * @return 对象键列表
     */
    public List<String> listObjects(String bucketName, String prefix) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName);
            
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            List<String> objects = response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
            log.info("列出对象: bucket={}, prefix={}, count={}", bucketName, prefix, objects.size());
            return objects;
        } catch (Exception e) {
            log.error("列出对象失败: bucket={}, prefix={}", bucketName, prefix, e);
            return List.of();
        }
    }

    /**
     * 列出对象（使用默认Bucket）
     * 
     * @param prefix 前缀过滤（可选）
     * @return 对象键列表
     */
    public List<String> listObjects(String prefix) {
        return listObjects(defaultBucket, prefix);
    }

    /**
     * 检查对象是否存在
     * 
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 是否存在
     */
    public boolean objectExists(String bucketName, String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查对象是否存在失败: bucket={}, key={}", bucketName, objectKey, e);
            return false;
        }
    }

    /**
     * 检查对象是否存在（使用默认Bucket）
     * 
     * @param objectKey 对象键
     * @return 是否存在
     */
    public boolean objectExists(String objectKey) {
        return objectExists(defaultBucket, objectKey);
    }

    /**
     * 获取对象元数据
     * 
     * @param bucketName Bucket名称（可为null，使用默认Bucket）
     * @param objectKey 对象键
     * @return 对象元数据
     */
    public HeadObjectResponse getObjectMetadata(String bucketName, String objectKey) {
        try {
            String actualBucket = bucketName != null ? bucketName : defaultBucket;
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(actualBucket)
                    .key(objectKey)
                    .build();
            return s3Client.headObject(request);
        } catch (Exception e) {
            log.error("获取对象元数据失败: bucket={}, key={}", bucketName, objectKey, e);
            return null;
        }
    }

    /**
     * 生成预签名URL（用于临时访问）
     * 
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @param expirationMinutes 过期时间（分钟）
     * @return 预签名URL
     */
    public String generatePresignedUrl(String bucketName, String objectKey, int expirationMinutes) {
        try {
            // 注意：预签名URL需要使用Presigner，这里简化处理
            // 实际使用时需要配置Presigner
            log.info("生成预签名URL: bucket={}, key={}, expiration={}分钟", 
                    bucketName, objectKey, expirationMinutes);
            // TODO: 实现预签名URL生成
            return null;
        } catch (Exception e) {
            log.error("生成预签名URL失败: bucket={}, key={}", bucketName, objectKey, e);
            return null;
        }
    }
}
