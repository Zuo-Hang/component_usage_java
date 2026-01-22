# ZooKeeper 使用示例

本模块演示如何使用 Apache Curator 框架操作 ZooKeeper。

## 功能特性

- ✅ 基本操作：创建、读取、更新、删除节点
- ✅ 节点类型：持久化节点、临时节点
- ✅ 节点监听：监听节点数据变化、子节点变化
- ✅ 使用 Apache Curator 框架（推荐的生产级客户端）

## 技术栈

- **Spring Boot 3.2.0**
- **Apache Curator 5.5.0** - ZooKeeper 客户端框架
- **ZooKeeper 3.9.1** - 分布式协调服务

## 快速开始

### 1. 启动 ZooKeeper

使用 Docker 启动 ZooKeeper：

```bash
docker run -d --name zookeeper \
  -p 2181:2181 \
  -e ZOO_MY_ID=1 \
  zookeeper:3.9
```

或者使用项目中的 docker-compose：

```bash
docker-compose up -d zookeeper
```

### 1.1 启动 ZooKeeper 可视化工具（可选）

项目已包含 ZooNavigator，可以通过 Web UI 可视化查看和管理 ZooKeeper：

```bash
docker-compose up -d zookeeper-navigator
```

访问地址：`http://localhost:9000`

在 ZooNavigator 中连接 ZooKeeper：
- **Connection String**: `zookeeper:2181`（容器内）或 `localhost:2181`（宿主机）
- 点击 "Connect" 即可查看 ZooKeeper 的目录结构

### 2. 启动应用

```bash
cd zookeeper-example
mvn spring-boot:run
```

应用将在 `http://localhost:8083` 启动。

## API 示例

### 创建持久化节点

```bash
curl -X POST "http://localhost:8083/zookeeper/create/persistent?path=/test/node1&data=hello"
```

### 创建临时节点

```bash
curl -X POST "http://localhost:8083/zookeeper/create/ephemeral?path=/test/temp1&data=temporary"
```

### 获取节点数据

```bash
curl "http://localhost:8083/zookeeper/get?path=/test/node1"
```

### 设置节点数据

```bash
curl -X PUT "http://localhost:8083/zookeeper/set?path=/test/node1&data=updated"
```

### 检查节点是否存在

```bash
curl "http://localhost:8083/zookeeper/exists?path=/test/node1"
```

### 获取子节点列表

```bash
curl "http://localhost:8083/zookeeper/children?path=/test"
```

### 删除节点

```bash
curl -X DELETE "http://localhost:8083/zookeeper/delete?path=/test/node1"
```

## 配置说明

在 `application.yml` 中可以配置 ZooKeeper 连接参数：

```yaml
zookeeper:
  connect-string: localhost:2181  # ZooKeeper 连接地址
  session-timeout: 30000          # 会话超时时间（毫秒）
  connection-timeout: 15000      # 连接超时时间（毫秒）
  retry-times: 3                  # 重试次数
  retry-interval: 1000            # 重试间隔（毫秒）
```

## 节点类型

### 持久化节点 (PERSISTENT)
- 节点创建后，即使客户端断开连接，节点仍然存在
- 适用于存储配置信息等需要持久化的数据

### 临时节点 (EPHEMERAL)
- 节点创建后，如果客户端断开连接，节点会被自动删除
- 适用于服务注册、临时状态等场景

## 监听机制

ZooKeeper 提供了两种监听方式：

1. **节点数据监听** - 监听节点数据的变化
2. **子节点监听** - 监听子节点的增加、删除

## 使用场景

- **配置管理** - 集中管理分布式系统的配置
- **服务注册与发现** - 实现服务注册中心
- **分布式锁** - 实现分布式锁机制
- **集群管理** - 管理集群节点状态
- **命名服务** - 提供统一的命名空间

## 注意事项

1. **连接管理** - 确保 ZooKeeper 客户端正确连接和关闭
2. **异常处理** - 网络异常、会话过期等需要妥善处理
3. **性能考虑** - 避免频繁创建和删除节点
4. **数据大小** - ZooKeeper 节点数据建议不超过 1MB

## 可视化工具

### ZooNavigator（推荐）

项目已配置 ZooNavigator，可以通过 Web UI 可视化查看和管理 ZooKeeper：

```bash
# 启动 ZooNavigator
docker-compose up -d zookeeper-navigator

# 访问 http://localhost:9000
```

**使用步骤：**
1. 确保 ZooKeeper 和 ZooNavigator 都已启动：
   ```bash
   docker-compose ps | grep zookeeper
   ```

2. 打开浏览器访问 `http://localhost:9000`

3. 在连接页面输入连接字符串：
   - **推荐使用**: `localhost:2181`（浏览器在宿主机上，使用 localhost 连接）
   - **如果失败，尝试**: `zookeeper:2181`（使用 Docker 服务名，某些配置下可能需要）

4. 点击 "Connect" 连接

5. 如果连接失败，检查：
   - ZooKeeper 是否正常运行：`docker logs zookeeper`
   - ZooKeeper 端口是否可访问：`nc -zv localhost 2181` 或 `telnet localhost 2181`
   - 网络是否正常：`docker network inspect component_usage_java_component-network`
   - 尝试重启服务：`docker-compose restart zookeeper zookeeper-navigator`

6. 连接成功后即可看到 ZooKeeper 的目录树结构，支持：
   - 浏览节点
   - 查看节点数据
   - 创建/删除节点
   - 编辑节点数据

**连接问题排查：**
- 如果使用 `localhost:2181` 连接失败，确保 ZooKeeper 的 2181 端口已正确映射
- 如果使用 `zookeeper:2181` 连接失败，检查 Docker 网络配置
- 可以先用命令行工具测试连接：`docker exec -it zookeeper zkCli.sh -server localhost:2181`

### 命令行工具

也可以使用 ZooKeeper 自带的命令行工具：

```bash
# 进入 ZooKeeper 容器
docker exec -it zookeeper bash

# 使用 zkCli.sh 连接
zkCli.sh -server localhost:2181

# 常用命令：
# ls /          # 列出根目录下的节点
# ls /test      # 列出 /test 下的子节点
# get /test     # 获取节点数据
# create /test/node "data"  # 创建节点
# delete /test/node  # 删除节点
```

## 参考文档

- [Apache Curator 官方文档](https://curator.apache.org/)
- [ZooKeeper 官方文档](https://zookeeper.apache.org/)
- [ZooNavigator GitHub](https://github.com/elkozmon/zoonavigator)