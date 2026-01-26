# S3 示例模块

本模块演示如何使用 S3 兼容的对象存储服务，支持 **MinIO**（本地）和 **AWS S3**。

## 功能特性

### 1. Bucket管理
- ✅ 创建Bucket
- ✅ 删除Bucket
- ✅ 列出所有Bucket

### 2. 文件操作
- ✅ 文件上传
- ✅ 文件下载
- ✅ 文件删除
- ✅ 列出对象
- ✅ 检查文件是否存在
- ✅ 获取文件元数据

### 3. 兼容性
- ✅ 支持MinIO（本地S3兼容服务）
- ✅ 支持AWS S3（云服务）

## 快速开始

### 1. 启动MinIO服务

使用 Docker Compose 启动 MinIO：

```bash
docker-compose up -d minio
```

MinIO 控制台地址：http://localhost:9001
- 默认用户名：`minioadmin`
- 默认密码：`minioadmin`

### 2. 创建Bucket

首次使用需要创建Bucket，可以通过以下方式：

**方式1：通过MinIO控制台**
1. 访问 http://localhost:9001
2. 登录后点击「Create Bucket」
3. 输入Bucket名称（如：`test-bucket`）

**方式2：通过API**
```bash
curl -X POST http://localhost:8086/s3/buckets/test-bucket
```

### 3. 启动应用

```bash
cd s3-example
mvn spring-boot:run
```

### 4. 测试文件上传

```bash
# 上传文件
curl -X POST http://localhost:8086/s3/upload \
  -F "file=@test.txt" \
  -F "key=test/test.txt"
```

## API 接口

### Bucket管理

#### 1. 创建Bucket
```bash
POST /s3/buckets/{bucketName}
```

示例：
```bash
curl -X POST http://localhost:8086/s3/buckets/test-bucket
```

响应：
```json
{
  "success": true,
  "bucketName": "test-bucket",
  "message": "Bucket创建成功"
}
```

#### 2. 删除Bucket
```bash
DELETE /s3/buckets/{bucketName}
```

#### 3. 列出所有Bucket
```bash
GET /s3/buckets
```

响应：
```json
{
  "success": true,
  "buckets": ["test-bucket", "another-bucket"],
  "count": 2
}
```

### 文件操作

#### 1. 上传文件
```bash
POST /s3/upload
Content-Type: multipart/form-data

参数：
- file: 文件（必需）
- bucket: Bucket名称（可选，使用默认Bucket）
- key: 对象键/文件路径（可选，使用文件名）
```

示例：
```bash
# 使用默认Bucket
curl -X POST http://localhost:8086/s3/upload \
  -F "file=@test.txt" \
  -F "key=test/test.txt"

# 指定Bucket
curl -X POST http://localhost:8086/s3/upload \
  -F "file=@test.txt" \
  -F "bucket=my-bucket" \
  -F "key=test/test.txt"
```

响应：
```json
{
  "success": true,
  "bucket": "test-bucket",
  "key": "test/test.txt",
  "fileName": "test.txt",
  "size": 1024,
  "contentType": "text/plain",
  "message": "文件上传成功"
}
```

#### 2. 下载文件
```bash
GET /s3/download?bucket=test-bucket&key=test/test.txt
```

示例：
```bash
# 使用默认Bucket
curl -O http://localhost:8086/s3/download?key=test/test.txt

# 指定Bucket
curl -O http://localhost:8086/s3/download?bucket=my-bucket&key=test/test.txt
```

#### 3. 删除文件
```bash
DELETE /s3/files?bucket=test-bucket&key=test/test.txt
```

示例：
```bash
curl -X DELETE "http://localhost:8086/s3/files?key=test/test.txt"
```

#### 4. 列出对象
```bash
GET /s3/list?bucket=test-bucket&prefix=test/
```

示例：
```bash
# 列出所有对象
curl http://localhost:8086/s3/list

# 列出指定前缀的对象
curl "http://localhost:8086/s3/list?prefix=test/"
```

响应：
```json
{
  "success": true,
  "bucket": "test-bucket",
  "prefix": "test/",
  "objects": ["test/file1.txt", "test/file2.txt"],
  "count": 2
}
```

#### 5. 检查文件是否存在
```bash
GET /s3/exists?bucket=test-bucket&key=test/test.txt
```

响应：
```json
{
  "success": true,
  "bucket": "test-bucket",
  "key": "test/test.txt",
  "exists": true
}
```

#### 6. 获取文件元数据
```bash
GET /s3/metadata?bucket=test-bucket&key=test/test.txt
```

响应：
```json
{
  "success": true,
  "bucket": "test-bucket",
  "key": "test/test.txt",
  "contentType": "text/plain",
  "contentLength": 1024,
  "lastModified": "2024-01-01T00:00:00Z",
  "etag": "\"abc123\""
}
```

## 配置说明

### application.yml 配置项

```yaml
s3:
  # 端点（MinIO需要，AWS S3可选）
  endpoint: http://localhost:9000
  
  # 访问密钥
  access-key: minioadmin
  secret-key: minioadmin
  
  # 区域
  region: us-east-1
  
  # 默认Bucket
  bucket: test-bucket
```

### MinIO配置

```yaml
s3:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  region: us-east-1
  bucket: test-bucket
```

### AWS S3配置

```yaml
s3:
  # 不配置endpoint，使用AWS默认端点
  # endpoint: https://s3.amazonaws.com
  access-key: ${AWS_ACCESS_KEY_ID}
  secret-key: ${AWS_SECRET_ACCESS_KEY}
  region: us-east-1
  bucket: my-aws-bucket
```

## 使用示例

### 1. 文件上传

```java
@Autowired
private S3Service s3Service;

// 上传文件
File file = new File("test.txt");
FileInputStream inputStream = new FileInputStream(file);
boolean success = s3Service.uploadFile("test-bucket", "test/test.txt", 
        inputStream, "text/plain");
```

### 2. 文件下载

```java
// 下载文件
byte[] fileBytes = s3Service.downloadFile("test-bucket", "test/test.txt");
// 保存到本地
Files.write(Paths.get("downloaded.txt"), fileBytes);
```

### 3. 列出对象

```java
// 列出所有对象
List<String> objects = s3Service.listObjects("test-bucket", null);

// 列出指定前缀的对象
List<String> objects = s3Service.listObjects("test-bucket", "test/");
```

## 核心概念

### 1. Bucket（存储桶）

- 类似于文件夹，用于组织对象
- 名称必须全局唯一（AWS S3）
- MinIO中可以在同一服务器上创建多个Bucket

### 2. Object Key（对象键）

- 对象的唯一标识符，类似于文件路径
- 例如：`images/2024/photo.jpg`
- 支持斜杠（/）作为路径分隔符

### 3. 区域（Region）

- AWS S3需要指定区域
- MinIO可以任意指定（通常使用`us-east-1`）

### 4. 访问控制

- 通过Access Key和Secret Key进行身份验证
- 可以配置Bucket和对象的访问权限

## MinIO vs AWS S3

| 特性 | MinIO | AWS S3 |
|------|-------|--------|
| **部署** | 本地/私有云 | 公有云 |
| **成本** | 免费（自托管） | 按使用量付费 |
| **端点** | 需要配置 | 自动选择 |
| **兼容性** | S3兼容 | 原生S3 |
| **适用场景** | 开发测试、私有部署 | 生产环境、云原生 |

## 最佳实践

### 1. Bucket命名

- ✅ 使用有意义的名称（如：`user-uploads`、`product-images`）
- ✅ 遵循命名规范（小写字母、数字、连字符）
- ✅ 避免使用敏感信息

### 2. 对象键设计

- ✅ 使用路径结构组织文件（如：`2024/01/photo.jpg`）
- ✅ 避免过深的嵌套（建议不超过3-4层）
- ✅ 使用有意义的文件名

### 3. 文件上传

- ✅ 设置合适的Content-Type
- ✅ 限制文件大小
- ✅ 验证文件类型
- ✅ 使用唯一文件名（如UUID）

### 4. 安全性

- ✅ 保护Access Key和Secret Key
- ✅ 使用环境变量存储敏感信息
- ✅ 配置Bucket访问策略
- ✅ 使用预签名URL进行临时访问

## 常见问题

### Q1: 连接MinIO失败？

**A**: 检查以下几点：
1. MinIO服务是否启动
2. `s3.endpoint` 配置是否正确（http://localhost:9000）
3. Access Key和Secret Key是否正确

### Q2: 上传文件失败？

**A**: 检查以下几点：
1. Bucket是否存在（不存在需要先创建）
2. 文件大小是否超过限制
3. 网络连接是否正常

### Q3: 如何切换到AWS S3？

**A**: 修改配置：
```yaml
s3:
  # 注释掉endpoint，使用AWS默认端点
  # endpoint: http://localhost:9000
  access-key: ${AWS_ACCESS_KEY_ID}
  secret-key: ${AWS_SECRET_ACCESS_KEY}
  region: us-east-1
  bucket: my-aws-bucket
```

## 参考资源

- [MinIO 官方文档](https://min.io/docs/)
- [AWS S3 文档](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [MinIO 控制台](http://localhost:9001)
