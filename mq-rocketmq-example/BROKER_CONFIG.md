# RocketMQ Broker 外部访问配置

## 问题说明

当 RocketMQ Broker 运行在 Docker 容器中时，默认会使用容器内部 IP（如 `172.17.0.3:10911`）向 NameServer 注册。这导致宿主机上的应用无法连接到 Broker。

## 解决方案

通过配置 `brokerIP1 = 127.0.0.1`，让 Broker 使用 `127.0.0.1:10911` 注册，这样宿主机上的应用就可以通过端口映射访问 Broker。

## 配置步骤

### 1. 创建 Broker 配置文件

```bash
mkdir -p ~/rocketmq/broker/conf
cat > ~/rocketmq/broker/conf/broker.conf << 'EOF'
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
# 关键配置：使用 127.0.0.1 让外部可以访问
brokerIP1 = 127.0.0.1
listenPort = 10911
EOF
```

### 2. 修改启动命令

在原有启动命令基础上，添加配置文件挂载和 `-c` 参数：

```bash
docker run -d --name rmqbroker \
  --platform linux/amd64 \
  -p 10909:10909 \
  -p 10911:10911 \
  -p 10912:10912 \
  -v ~/rocketmq/broker/logs:/home/rocketmq/logs \
  -v ~/rocketmq/broker/store:/home/rocketmq/store \
  -v ~/rocketmq/broker/conf/broker.conf:/home/rocketmq/rocketmq-5.4.0/conf/broker.conf \
  -e NAMESRV_ADDR=rmqnamesrv:9876 \
  -e JAVA_OPT_EXT="-Xms512M -Xmx512M -Xmn128m" \
  --link rmqnamesrv \
  apache/rocketmq:latest \
  sh mqbroker -n rmqnamesrv:9876 -c /home/rocketmq/rocketmq-5.4.0/conf/broker.conf
```

### 3. 关键修改点

1. **添加配置文件挂载**：
   ```bash
   -v ~/rocketmq/broker/conf/broker.conf:/home/rocketmq/rocketmq-5.4.0/conf/broker.conf
   ```

2. **配置文件内容**：
   - `brokerIP1 = 127.0.0.1` - 让 Broker 使用 localhost 注册
   - `listenPort = 10911` - 指定监听端口

3. **启动命令添加 `-c` 参数**：
   ```bash
   sh mqbroker -n rmqnamesrv:9876 -c /home/rocketmq/rocketmq-5.4.0/conf/broker.conf
   ```

## 验证配置

### 1. 检查 Broker 启动日志

```bash
docker logs rmqbroker --tail 10
```

应该看到：
```
The broker[broker-a, 127.0.0.1:10911] boot success. serializeType=JSON and name server is rmqnamesrv:9876
```

注意：IP 地址应该是 `127.0.0.1:10911`，而不是 `172.17.0.3:10911`。

### 2. 检查集群信息

```bash
docker exec rmqnamesrv sh -c "cd /home/rocketmq/rocketmq-5.4.0/bin && sh mqadmin clusterList -n localhost:9876"
```

应该看到 Broker 的 IP 是 `127.0.0.1:10911`。

### 3. 测试消息发送

```bash
# 同步消息
curl -X POST "http://localhost:8082/mq/sync?orderId=10001"

# 异步消息
curl -X POST "http://localhost:8082/mq/async?orderId=10002"

# 单向消息
curl -X POST "http://localhost:8082/mq/oneway?orderId=10003"
```

## 配置说明

- **brokerIP1**: Broker 向 NameServer 注册的 IP 地址。设置为 `127.0.0.1` 后，客户端从 NameServer 获取的 Broker 地址就是 `127.0.0.1:10911`，可以通过 Docker 端口映射访问。
- **listenPort**: Broker 监听的端口，需要与 Docker 端口映射一致。

## 注意事项

1. 配置文件路径需要根据 RocketMQ 版本调整（如 `rocketmq-5.4.0` 可能需要改为实际版本号）
2. 如果使用 Docker Compose，可以在 `command` 中添加 `-c` 参数
3. 确保端口映射正确：`-p 10911:10911`

## 完整示例（Docker Compose）

如果使用 Docker Compose，可以这样配置：

```yaml
rocketmq-broker:
  image: apache/rocketmq:latest
  container_name: rmqbroker
  ports:
    - "10909:10909"
    - "10911:10911"
    - "10912:10912"
  volumes:
    - ~/rocketmq/broker/logs:/home/rocketmq/logs
    - ~/rocketmq/broker/store:/home/rocketmq/store
    - ~/rocketmq/broker/conf/broker.conf:/home/rocketmq/rocketmq-5.4.0/conf/broker.conf
  environment:
    NAMESRV_ADDR: rmqnamesrv:9876
    JAVA_OPT_EXT: "-Xms512M -Xmx512M -Xmn128m"
  command: sh mqbroker -n rmqnamesrv:9876 -c /home/rocketmq/rocketmq-5.4.0/conf/broker.conf
  depends_on:
    - rmqnamesrv
```
