# ZooNavigator 连接问题排查

## 问题：Unable to establish connection with ZooKeeper (localhost:2181)

### 解决方案

#### 1. 确认 ZooKeeper 已完全启动

```bash
# 检查 ZooKeeper 状态
docker ps | grep zookeeper

# 等待 ZooKeeper 完全启动（通常需要 10-30 秒）
docker logs zookeeper --tail 20

# 验证 ZooKeeper 是否可以连接
docker exec -it zookeeper zkCli.sh -server localhost:2181
# 输入: ls /
# 如果能看到节点列表，说明 ZooKeeper 正常
```

#### 2. 在 ZooNavigator 中正确连接

1. **打开浏览器访问**: `http://localhost:9000`

2. **在连接页面输入连接字符串**:
   - ✅ **推荐**: `localhost:2181`
   - ⚠️ **不要使用**: `zookeeper:2181`（这是 Docker 网络内的服务名，浏览器无法解析）

3. **点击 "Connect" 按钮**

#### 3. 如果仍然无法连接

**检查端口是否可访问：**
```bash
# 测试端口
nc -zv localhost 2181
# 或
telnet localhost 2181
```

**检查防火墙：**
- macOS: 检查系统防火墙设置
- Linux: 检查 iptables/firewalld

**检查端口占用：**
```bash
lsof -i :2181
```

**重启服务：**
```bash
docker-compose restart zookeeper zookeeper-navigator
```

#### 4. 使用命令行工具验证

如果 Web UI 无法连接，可以使用命令行工具：

```bash
# 进入 ZooKeeper 容器
docker exec -it zookeeper bash

# 使用 zkCli.sh 连接
zkCli.sh -server localhost:2181

# 常用命令：
# ls /          # 列出根目录
# ls /test      # 列出 /test 下的节点
# get /test     # 获取节点数据
# create /test/node "data"  # 创建节点
# delete /test/node  # 删除节点
```

### 常见错误

1. **Connection refused**
   - ZooKeeper 未启动或未完全启动
   - 端口映射错误
   - 防火墙阻止

2. **Timeout**
   - 网络问题
   - ZooKeeper 负载过高
   - 会话超时设置过短

3. **Authentication failed**
   - ZooKeeper 配置了 ACL（访问控制列表）
   - 需要提供用户名和密码

### 验证步骤

1. ✅ ZooKeeper 容器运行中
2. ✅ 端口 2181 可访问
3. ✅ 命令行工具可以连接
4. ✅ ZooNavigator 容器运行中
5. ✅ 浏览器可以访问 http://localhost:9000
6. ✅ 在 ZooNavigator 中输入 `localhost:2181` 连接

### 备用方案

如果 ZooNavigator 无法使用，可以考虑：

1. **zkui** - 另一个 ZooKeeper Web UI
2. **PrettyZoo** - 桌面应用（需要下载安装）
3. **命令行工具** - zkCli.sh（最可靠）
