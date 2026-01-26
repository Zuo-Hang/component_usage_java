# Etcd 示例模块

本模块演示如何在生产环境中使用 Etcd 分布式键值存储，实现服务发现和配置管理。

## ⚠️ 重要说明

**Etcd 是分布式键值存储系统，主要用于：**
- 服务发现
- 配置管理
- 分布式协调
- 分布式锁

### 适用场景 ✅
- 微服务架构中的服务注册与发现
- 分布式配置管理
- 需要自动过期功能的临时数据
- 分布式锁和选举

### 不适用场景 ❌
- 大规模数据存储（Etcd 不是数据库）
- 频繁的大数据量读写（性能有限）

## Etcd vs ZooKeeper

| 特性 | ZooKeeper | Etcd |
|------|-----------|------|
| **协议** | TCP（自定义协议） | HTTP/gRPC |
| **一致性算法** | ZAB | Raft |
| **租约/TTL** | 临时节点 | ✅ 租约（Lease）和TTL |
| **Watch机制** | ✅ 支持 | ✅ 支持 |
| **服务发现** | ✅ 支持 | ✅ 支持 |
| **配置管理** | ✅ 支持 | ✅ 支持 |
| **性能** | 中等 | 较高 |
| **易用性** | 中等 | ✅ 更易用（HTTP/gRPC） |

## 功能特性

### 1. 分布式键值存储

- ✅ PUT - 写入键值对
- ✅ GET - 读取键值对
- ✅ DELETE - 删除键值对
- ✅ 前缀查询
- ✅ 批量操作

### 2. 服务发现

- ✅ 服务注册（带租约，自动续约）
- ✅ 服务发现（查询服务实例）
- ✅ 服务监听（监听服务上下线）
- ✅ 服务注销

### 3. 配置管理

- ✅ 配置写入
- ✅ 配置读取
- ✅ 配置监听（配置变更通知）
- ✅ 批量配置管理

### 4. 租约（Lease）和TTL

- ✅ 创建租约（带TTL）
- ✅ 续约（KeepAlive）
- ✅ 撤销租约
- ✅ 查询租约信息
- ✅ 自动续约

## 快速开始

### 1. 启动 Etcd 服务

使用 Docker Compose 启动 Etcd：

```bash
docker-compose up -d etcd
```

Etcd 连接信息：
- 客户端 API 端口：`2379`
- 节点间通信端口：`2380`
- 默认无认证（生产环境需要配置认证）

### 2. 启动应用

```bash
cd etcd-example
mvn spring-boot:run
```

### 3. 测试键值存储

```bash
# 写入键值对
curl -X PUT "http://localhost:8090/etcd/kv/test-key?value=test-value"

# 读取键值对
curl http://localhost:8090/etcd/kv/test-key

# 删除键值对
curl -X DELETE http://localhost:8090/etcd/kv/test-key
```

### 4. 测试服务发现

```bash
# 注册服务
curl -X POST "http://localhost:8090/etcd/service/register?serviceName=user-service&instanceId=instance-1&instanceInfo={\"host\":\"localhost\",\"port\":8080}&ttl=60"

# 发现服务
curl http://localhost:8090/etcd/service/discover/user-service

# 获取所有服务
curl http://localhost:8090/etcd/service/list
```

## API 接口

### 键值存储

#### 1. 写入键值对
```bash
PUT /etcd/kv/{key}?value=xxx
```

#### 2. 读取键值对
```bash
GET /etcd/kv/{key}
```

#### 3. 删除键值对
```bash
DELETE /etcd/kv/{key}
```

#### 4. 前缀查询
```bash
GET /etcd/kv/prefix/{prefix}
```

#### 5. 批量写入
```bash
POST /etcd/kv/batch
Content-Type: application/json

{
  "key1": "value1",
  "key2": "value2"
}
```

### 服务发现

#### 1. 服务注册
```bash
POST /etcd/service/register?serviceName=xxx&instanceId=xxx&instanceInfo=xxx&ttl=60
```

**参数说明**：
- `serviceName`: 服务名称
- `instanceId`: 实例ID
- `instanceInfo`: 实例信息（JSON格式，包含host、port等）
- `ttl`: 租约TTL（秒），服务会定期续约

**响应示例**：
```json
{
  "success": true,
  "serviceName": "user-service",
  "instanceId": "instance-1",
  "leaseId": 1234567890,
  "message": "服务注册成功，租约自动续约"
}
```

#### 2. 服务注销
```bash
DELETE /etcd/service/unregister?serviceName=xxx&instanceId=xxx
```

#### 3. 发现服务实例
```bash
GET /etcd/service/discover/{serviceName}
```

#### 4. 获取所有服务
```bash
GET /etcd/service/list
```

#### 5. 获取服务的第一个可用实例
```bash
GET /etcd/service/first/{serviceName}
```

### 配置管理

#### 1. 写入配置
```bash
PUT /etcd/config/{application}/{key}?value=xxx
```

#### 2. 读取配置
```bash
GET /etcd/config/{application}/{key}
```

#### 3. 删除配置
```bash
DELETE /etcd/config/{application}/{key}
```

#### 4. 获取应用的所有配置
```bash
GET /etcd/config/{application}
```

#### 5. 批量写入配置
```bash
POST /etcd/config/{application}/batch
Content-Type: application/json

{
  "database.url": "jdbc:mysql://localhost:3306/test",
  "database.username": "root",
  "database.password": "password"
}
```

### 租约操作

#### 1. 创建租约
```bash
POST /etcd/lease/grant?ttl=60
```

#### 2. 续约
```bash
POST /etcd/lease/keepalive/{leaseId}
```

#### 3. 撤销租约
```bash
DELETE /etcd/lease/{leaseId}
```

#### 4. 查询租约信息
```bash
GET /etcd/lease/{leaseId}
```

## 配置说明

### application.yml 配置项

```yaml
etcd:
  # Etcd服务地址（支持多节点，逗号分隔）
  endpoints: http://localhost:2379
  
  # 服务发现配置
  service:
    prefix: /services  # 服务注册前缀
  
  # 配置管理配置
  config:
    prefix: /config  # 配置存储前缀
```

### 多节点配置

```yaml
etcd:
  endpoints: http://etcd1:2379,http://etcd2:2379,http://etcd3:2379
```

## 生产环境最佳实践

### 1. 服务注册

#### 使用租约自动续约
```java
// 注册服务时指定TTL，系统会自动续约
long leaseId = serviceDiscoveryService.registerService(
    "user-service", 
    "instance-1", 
    "{\"host\":\"localhost\",\"port\":8080}", 
    60  // TTL 60秒
);
```

**优势**：
- 服务下线时自动删除（租约过期）
- 无需手动注销
- 支持服务健康检查

### 2. 服务发现

#### 监听服务变化
```java
serviceDiscoveryService.watchService("user-service", instances -> {
    log.info("服务实例变化: {}", instances);
    // 更新本地服务列表
    updateLocalServiceList(instances);
});
```

**优势**：
- 实时感知服务上下线
- 自动更新服务列表
- 无需轮询

### 3. 配置管理

#### 监听配置变化
```java
configService.watchConfig("myapp", configs -> {
    log.info("配置变化: {}", configs);
    // 重新加载配置
    reloadConfig(configs);
});
```

**优势**：
- 配置变更实时通知
- 支持动态配置更新
- 无需重启应用

### 4. 租约管理

#### 自动续约
```java
// 创建租约
long leaseId = leaseService.grantLease(60);

// 启动自动续约（持续续约，直到租约被撤销）
leaseService.startAutoKeepAlive(leaseId);
```

**优势**：
- 自动续约，无需手动管理
- 服务异常退出时自动过期
- 适合临时数据存储

### 5. 键值存储

#### 前缀查询
```java
// 获取所有以 /services/user-service/ 开头的键值对
List<KeyValue> instances = keyValueService.getByPrefix("/services/user-service/");
```

**优势**：
- 高效的前缀查询
- 适合服务发现场景
- 支持批量操作

## 典型使用场景

### 1. 微服务服务发现

```java
// 服务启动时注册
@PostConstruct
public void register() {
    String instanceInfo = String.format(
        "{\"host\":\"%s\",\"port\":%d,\"version\":\"1.0.0\"}", 
        getHost(), getPort()
    );
    serviceDiscoveryService.registerService(
        "user-service", 
        getInstanceId(), 
        instanceInfo, 
        60
    );
}

// 服务关闭时注销
@PreDestroy
public void unregister() {
    serviceDiscoveryService.unregisterService("user-service", getInstanceId());
}
```

### 2. 分布式配置管理

```java
// 启动时监听配置变化
@PostConstruct
public void watchConfig() {
    configService.watchConfig("myapp", configs -> {
        // 更新应用配置
        updateApplicationConfig(configs);
    });
}

// 读取配置
String dbUrl = configService.getConfig("myapp", "database.url");
```

### 3. 临时数据存储（带TTL）

```java
// 写入临时数据（60秒后自动过期）
long leaseId = leaseService.grantLease(60);
keyValueService.putWithLease("/temp/data", "value", leaseId);
```

### 4. 分布式锁（基于租约）

```java
// 创建租约作为锁
long leaseId = leaseService.grantLease(30);  // 锁30秒

// 尝试获取锁
if (keyValueService.put("/lock/resource", "locked", leaseId)) {
    try {
        // 执行业务逻辑
        doBusinessLogic();
    } finally {
        // 释放锁（撤销租约）
        leaseService.revokeLease(leaseId);
    }
}
```

## Etcd vs ZooKeeper 详细对比

### 1. 协议和易用性

**Etcd**：
- ✅ HTTP/gRPC 协议，易于使用
- ✅ RESTful API
- ✅ 支持多种客户端

**ZooKeeper**：
- ⚠️ TCP 自定义协议
- ⚠️ 需要专用客户端（如 Curator）

### 2. 租约和TTL

**Etcd**：
- ✅ 支持租约（Lease）和TTL
- ✅ 自动过期
- ✅ 灵活的TTL设置

**ZooKeeper**：
- ⚠️ 使用临时节点
- ⚠️ 需要保持连接
- ⚠️ 连接断开时节点删除

### 3. 性能

**Etcd**：
- ✅ 较高的读写性能
- ✅ 支持批量操作

**ZooKeeper**：
- ⚠️ 性能中等
- ⚠️ 写操作需要多数节点确认

### 4. 一致性算法

**Etcd**：
- ✅ Raft 算法（易于理解）

**ZooKeeper**：
- ⚠️ ZAB 算法（较复杂）

### 5. 使用场景

| 场景 | ZooKeeper | Etcd |
|------|-----------|------|
| **服务发现** | ✅ 支持 | ✅ 支持（更易用） |
| **配置管理** | ✅ 支持 | ✅ 支持（更易用） |
| **分布式锁** | ✅ 支持 | ✅ 支持（基于租约） |
| **临时数据** | ✅ 临时节点 | ✅ 租约+TTL（更灵活） |

## 常见问题

### Q1: Etcd 和 ZooKeeper 如何选择？

**A**: 
- **选择 Etcd**：需要 HTTP/gRPC 协议、更易用的 API、租约和TTL功能
- **选择 ZooKeeper**：已有 ZooKeeper 集群、使用 Hadoop 生态

### Q2: 服务注册后如何保证服务在线？

**A**: 
- 使用租约自动续约
- 服务注册时指定 TTL
- 系统会自动续约，服务异常退出时租约过期，服务自动注销

### Q3: 如何监听配置变化？

**A**: 
```java
configService.watchConfig("myapp", configs -> {
    // 配置变化时的回调
    reloadConfig(configs);
});
```

### Q4: Etcd 的性能如何？

**A**: 
- 单节点：每秒数千次读写
- 集群：性能随节点数增加而提升
- 适合配置管理和服务发现场景
- 不适合大规模数据存储

### Q5: 如何实现分布式锁？

**A**: 
```java
// 1. 创建租约（作为锁的TTL）
long leaseId = leaseService.grantLease(30);

// 2. 尝试写入锁键（带租约）
if (keyValueService.putWithLease("/lock/resource", "locked", leaseId)) {
    // 3. 获取锁成功，执行业务逻辑
    try {
        doBusinessLogic();
    } finally {
        // 4. 释放锁（撤销租约）
        leaseService.revokeLease(leaseId);
    }
}
```

## 参考资源

- [Etcd 官方文档](https://etcd.io/docs/)
- [Etcd Java Client (jetcd)](https://github.com/etcd-io/jetcd)
- [Etcd vs ZooKeeper 对比](https://etcd.io/docs/learning/why/)
