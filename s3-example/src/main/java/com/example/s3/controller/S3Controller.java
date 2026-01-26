package com.example.s3.controller;

import com.example.s3.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * S3操作控制器
 * 提供文件上传、下载、删除等REST API
 */
@Slf4j
@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    // ========== Bucket管理接口 ==========

    /**
     * 创建Bucket
     * POST /s3/buckets/{bucketName}
     */
    @PostMapping("/buckets/{bucketName}")
    public Map<String, Object> createBucket(@PathVariable String bucketName) {
        boolean success = s3Service.createBucket(bucketName);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("bucketName", bucketName);
        response.put("message", success ? "Bucket创建成功" : "Bucket已存在或创建失败");
        return response;
    }

    /**
     * 删除Bucket
     * DELETE /s3/buckets/{bucketName}
     */
    @DeleteMapping("/buckets/{bucketName}")
    public Map<String, Object> deleteBucket(@PathVariable String bucketName) {
        boolean success = s3Service.deleteBucket(bucketName);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("bucketName", bucketName);
        response.put("message", success ? "Bucket删除成功" : "Bucket删除失败");
        return response;
    }

    /**
     * 列出所有Bucket
     * GET /s3/buckets
     */
    @GetMapping("/buckets")
    public Map<String, Object> listBuckets() {
        List<String> buckets = s3Service.listBuckets();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("buckets", buckets);
        response.put("count", buckets.size());
        return response;
    }

    // ========== 文件操作接口 ==========

    /**
     * 上传文件
     * POST /s3/upload?bucket=test-bucket&key=test/file.txt
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String key) {
        
        try {
            // 如果没有指定key，使用文件名
            String objectKey = key != null ? key : file.getOriginalFilename();
            String bucketName = bucket != null ? bucket : null;
            
            boolean success;
            if (bucketName != null) {
                success = s3Service.uploadFile(bucketName, objectKey, 
                        file.getInputStream(), file.getContentType());
            } else {
                success = s3Service.uploadFile(objectKey, 
                        file.getInputStream(), file.getContentType());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("bucket", bucketName);
            response.put("key", objectKey);
            response.put("fileName", file.getOriginalFilename());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());
            response.put("message", success ? "文件上传成功" : "文件上传失败");
            return response;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 下载文件
     * GET /s3/download?bucket=test-bucket&key=test/file.txt
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam(required = false) String bucket,
            @RequestParam String key) {
        
        try {
            byte[] fileBytes;
            if (bucket != null) {
                fileBytes = s3Service.downloadFile(bucket, key);
            } else {
                fileBytes = s3Service.downloadFile(key);
            }
            
            if (fileBytes == null) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", key);
            headers.setContentLength(fileBytes.length);
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除文件
     * DELETE /s3/files?bucket=test-bucket&key=test/file.txt
     */
    @DeleteMapping("/files")
    public Map<String, Object> deleteFile(
            @RequestParam(required = false) String bucket,
            @RequestParam String key) {
        
        boolean success;
        if (bucket != null) {
            success = s3Service.deleteFile(bucket, key);
        } else {
            success = s3Service.deleteFile(key);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("bucket", bucket);
        response.put("key", key);
        response.put("message", success ? "文件删除成功" : "文件删除失败");
        return response;
    }

    /**
     * 列出对象
     * GET /s3/list?bucket=test-bucket&prefix=test/
     */
    @GetMapping("/list")
    public Map<String, Object> listObjects(
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String prefix) {
        
        List<String> objects;
        if (bucket != null) {
            objects = s3Service.listObjects(bucket, prefix);
        } else {
            objects = s3Service.listObjects(prefix);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("bucket", bucket);
        response.put("prefix", prefix);
        response.put("objects", objects);
        response.put("count", objects.size());
        return response;
    }

    /**
     * 检查文件是否存在
     * GET /s3/exists?bucket=test-bucket&key=test/file.txt
     */
    @GetMapping("/exists")
    public Map<String, Object> checkFileExists(
            @RequestParam(required = false) String bucket,
            @RequestParam String key) {
        
        boolean exists;
        if (bucket != null) {
            exists = s3Service.objectExists(bucket, key);
        } else {
            exists = s3Service.objectExists(key);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("bucket", bucket);
        response.put("key", key);
        response.put("exists", exists);
        return response;
    }

    /**
     * 获取文件元数据
     * GET /s3/metadata?bucket=test-bucket&key=test/file.txt
     */
    @GetMapping("/metadata")
    public Map<String, Object> getFileMetadata(
            @RequestParam(required = false) String bucket,
            @RequestParam String key) {
        
        HeadObjectResponse metadata;
        if (bucket != null) {
            metadata = s3Service.getObjectMetadata(bucket, key);
        } else {
            metadata = s3Service.getObjectMetadata(null, key);
        }
        
        Map<String, Object> response = new HashMap<>();
        if (metadata != null) {
            response.put("success", true);
            response.put("bucket", bucket);
            response.put("key", key);
            response.put("contentType", metadata.contentType());
            response.put("contentLength", metadata.contentLength());
            response.put("lastModified", metadata.lastModified());
            response.put("etag", metadata.eTag());
        } else {
            response.put("success", false);
            response.put("message", "文件不存在或获取元数据失败");
        }
        return response;
    }

    /**
     * 健康检查
     * GET /s3/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "s3-example");
        return response;
    }
}
