# Nacos 示例模块

本模块演示如何使用 Nacos 进行服务注册与发现、配置管理。

## 功能特性

### 1. 服务注册与发现
- ✅ 服务自动注册到 Nacos
- ✅ 服务发现（查询已注册的服务）
- ✅ 服务实例查询（获取服务的所有实例）
- ✅ 服务健康检查
- ✅ 服务订阅（监听服务上下线）

### 2. 配置管理
- ✅ 从 Nacos 配置中心读取配置
- ✅ 配置动态刷新（@RefreshScope）
- ✅ 配置发布/删除
- ✅ 配置变化监听

## 快速开始

### 1. 启动 Nacos 服务

使用 Docker Compose 启动 Nacos：

```bash
docker-compose up -d nacos
```

Nacos 控制台地址：http://localhost:8848/nacos
- 默认用户名：`nacos`
- 默认密码：`nacos`

### 2. 启动应用

```bash
cd nacos-example
mvn spring-boot:run
```

应用启动后会自动注册到 Nacos。

### 3. 验证服务注册

访问 Nacos 控制台：http://localhost:8848/nacos
- 进入「服务管理」->「服务列表」
- 可以看到 `nacos-example` 服务已注册

## API 接口

### 服务发现相关

#### 1. 获取所有已注册的服务
```bash
GET /nacos/services
```

响应示例：
```json
{
  "success": true,
  "services": ["nacos-example", "other-service"],
  "count": 2
}
```

#### 2. 获取指定服务的实例列表
```bash
GET /nacos/services/{serviceName}/instances
```

示例：
```bash
curl http://localhost:8085/nacos/services/nacos-example/instances
```

响应示例：
```json
{
  "success": true,
  "serviceName": "nacos-example",
  "instances": [
    {
      "host": "192.168.1.100",
      "port": 8085,
      "serviceId": "nacos-example",
      "metadata": {
        "version": "1.0.0"
      }
    }
  ],
  "count": 1
}
```

#### 3. 获取服务的第一个可用实例
```bash
GET /nacos/services/{serviceName}/instance
```

#### 4. 构建服务调用URL
```bash
GET /nacos/services/{serviceName}/url?path=/api/test
```

### 配置管理相关

#### 1. 获取应用配置信息
```bash
GET /nacos/config/app
```

响应示例：
```json
{
  "success": true,
  "config": {
    "name": "nacos-example",
    "version": "1.0.0",
    "description": "Nacos示例应用"
  }
}
```

#### 2. 获取配置
```bash
GET /nacos/config?dataId=test.properties&group=DEFAULT_GROUP
```

#### 3. 发布配置
```bash
POST /nacos/config
Content-Type: application/json

{
  "dataId": "test.properties",
  "group": "DEFAULT_GROUP",
  "content": "app.name=test\napp.version=2.0.0"
}
```

#### 4. 删除配置
```bash
DELETE /nacos/config?dataId=test.properties&group=DEFAULT_GROUP
```

## 配置说明

### application.yml 配置项

```yaml
spring:
  cloud:
    nacos:
      # 服务注册与发现
      discovery:
        server-addr: localhost:8848  # Nacos服务器地址
        namespace:                   # 命名空间（可选）
        group: DEFAULT_GROUP         # 服务组（可选）
        weight: 1.0                  # 权重（0-1）
        ephemeral: true              # 是否临时实例
      
      # 配置管理
      config:
        server-addr: localhost:8848  # Nacos服务器地址
        namespace:                   # 命名空间（可选）
        group: DEFAULT_GROUP         # 配置组（可选）
        file-extension: properties   # 配置文件扩展名
```

## 使用示例

### 1. 服务注册

应用启动后会自动注册到 Nacos，无需额外代码。

### 2. 服务发现

```java
@Autowired
private DiscoveryClient discoveryClient;

// 获取所有服务
List<String> services = discoveryClient.getServices();

// 获取服务实例
List<ServiceInstance> instances = discoveryClient.getInstances("nacos-example");
```

### 3. 配置读取

在代码中使用 `@Value` 注入配置：

```java
@Value("${app.name}")
private String appName;

@Value("${app.version}")
private String appVersion;
```

### 4. 配置动态刷新

使用 `@RefreshScope` 注解使配置支持动态刷新：

```java
@Service
@RefreshScope
public class ConfigService {
    @Value("${app.name}")
    private String appName;
    
    // 当Nacos中的配置更新时，appName会自动刷新
}
```

### 5. 在 Nacos 控制台管理配置

1. 登录 Nacos 控制台：http://localhost:8848/nacos
2. 进入「配置管理」->「配置列表」
3. 点击「+」创建配置：
   - **Data ID**: `nacos-example.properties`
   - **Group**: `DEFAULT_GROUP`
   - **配置格式**: `Properties`
   - **配置内容**:
     ```properties
     app.name=nacos-example
     app.version=1.0.0
     app.description=Nacos示例应用
     ```
4. 点击「发布」，配置会立即生效

## 核心概念

### 1. 服务注册与发现

- **服务注册**：应用启动时向 Nacos 注册自己的服务信息
- **服务发现**：通过服务名称查找可用的服务实例
- **健康检查**：Nacos 会定期检查服务实例的健康状态
- **服务订阅**：监听服务实例的变化（上下线）

### 2. 配置管理

- **Data ID**：配置的唯一标识，格式：`${spring.application.name}.${file-extension}`
- **Group**：配置组，用于区分不同环境的配置
- **Namespace**：命名空间，用于多租户隔离
- **动态刷新**：配置更新后，应用可以自动获取最新配置

### 3. 命名空间（Namespace）

用于多环境隔离：
- **开发环境**：`dev`
- **测试环境**：`test`
- **生产环境**：`prod`

配置方式：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        namespace: dev  # 开发环境
      config:
        namespace: dev
```

## 最佳实践

### 1. 服务注册

- ✅ 使用有意义的服务名称（如：`user-service`、`order-service`）
- ✅ 设置合适的权重（用于负载均衡）
- ✅ 添加元数据信息（版本、区域等）

### 2. 配置管理

- ✅ 使用命名空间区分不同环境
- ✅ 敏感配置使用加密
- ✅ 配置变更后及时验证
- ✅ 使用配置分组管理不同模块的配置

### 3. 服务发现

- ✅ 使用负载均衡器（如 Spring Cloud LoadBalancer）
- ✅ 处理服务不可用的情况
- ✅ 实现服务降级和熔断

## 常见问题

### Q1: 服务注册失败？

**A**: 检查以下几点：
1. Nacos 服务是否启动
2. `spring.cloud.nacos.discovery.server-addr` 配置是否正确
3. 网络是否连通

### Q2: 配置读取不到？

**A**: 检查以下几点：
1. Data ID 是否正确（格式：`${spring.application.name}.properties`）
2. Group 是否匹配
3. 命名空间是否一致
4. 配置是否已发布

### Q3: 配置不刷新？

**A**: 确保：
1. 使用了 `@RefreshScope` 注解
2. 配置变更后已发布
3. 应用已连接到 Nacos

## 参考资源

- [Nacos 官方文档](https://nacos.io/docs/latest/what-is-nacos/)
- [Spring Cloud Alibaba 文档](https://github.com/alibaba/spring-cloud-alibaba)
- [Nacos 控制台](http://localhost:8848/nacos)
