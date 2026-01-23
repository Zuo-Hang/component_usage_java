# Docker Compose 使用说明

## 什么是 Docker Compose？

Docker Compose 是一个用于定义和运行多容器 Docker 应用程序的工具。通过一个 YAML 文件配置所有服务，然后用一条命令启动所有服务。

### 主要优势

1. **一键启动所有依赖服务**：无需手动安装和配置 Redis、MySQL、RocketMQ
2. **环境隔离**：每个项目有独立的环境，互不干扰
3. **配置统一管理**：所有服务配置在一个文件中
4. **快速重置**：可以快速删除和重建所有服务

## 本项目的 Docker Compose 配置

本项目使用 Docker Compose 管理以下服务：

- **Redis** (端口 6379) - 用于 `redis-example` 模块
- **MySQL** (端口 3306) - 用于 `mysql-example` 模块
- **RocketMQ NameServer** (端口 9876) - 用于 `mq-rocketmq-example` 模块
- **RocketMQ Broker** (端口 10909, 10911) - 用于 `mq-rocketmq-example` 模块
- **RocketMQ Console** (端口 8080) - RocketMQ 管理控制台（可选）

## 快速开始

### 1. 启动所有服务

```bash
# 在项目根目录执行
docker-compose up -d
```

`-d` 参数表示在后台运行（detached mode）

**注意**：如果遇到镜像拉取超时问题，请参考下面的故障排查部分

### 1.1 如果网络有问题，先启动 Redis 和 MySQL

```bash
# 只启动 Redis 和 MySQL（这两个通常没问题）
docker-compose up -d redis mysql

# 或者使用简化版配置
docker-compose -f docker-compose.simple.yml up -d
```

### 2. 查看服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f

# 查看特定服务的日志
docker-compose logs -f redis
docker-compose logs -f mysql
docker-compose logs -f rocketmq-nameserver
```

### 3. 停止所有服务

```bash
# 停止服务（保留数据卷）
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 停止并删除容器和数据卷（完全清理）
docker-compose down -v
```

### 4. 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart redis
```

## 服务访问信息

### Redis
- **地址**: localhost:6379
- **密码**: 无
- **数据库**: 0

### MySQL
- **地址**: localhost:3306
- **用户名**: root
- **密码**: root
- **数据库**: test_db
- **自动执行**: schema.sql 会在首次启动时自动执行

### RocketMQ
- **NameServer地址**: localhost:9876
- **控制台**: http://localhost:8080

## 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 查看日志
docker-compose logs -f [service_name]

# 进入容器
docker-compose exec redis sh
docker-compose exec mysql bash

# 查看服务状态
docker-compose ps

# 重新构建（如果修改了配置）
docker-compose up -d --force-recreate

# 查看资源使用情况
docker stats
```

## 数据持久化

所有数据都存储在 Docker 数据卷中，即使删除容器，数据也不会丢失：

- `redis-data`: Redis 数据
- `mysql-data`: MySQL 数据
- `rocketmq-*-logs`: RocketMQ 日志
- `rocketmq-broker-store`: RocketMQ 消息存储

## 健康检查

所有服务都配置了健康检查，可以通过以下命令查看：

```bash
docker-compose ps
```

状态显示为 `healthy` 表示服务正常运行。

## 故障排查

### 1. 镜像拉取超时

如果遇到 `TLS handshake timeout` 或网络问题：

**解决方案**：
- 配置 Docker 镜像加速器（推荐）
  - macOS: Docker Desktop → Settings → Docker Engine
  - 添加镜像加速器地址：
    ```json
    {
      "registry-mirrors": [
        "https://docker.mirrors.ustc.edu.cn",
        "https://hub-mirror.c.163.com",
        "https://mirror.baidubce.com"
      ]
    }
    ```
  - 点击 Apply & Restart
- 使用简化版配置（只启动 Redis 和 MySQL）：
  ```bash
  docker-compose -f docker-compose.simple.yml up -d
  ```
- 多次重试拉取镜像：
  ```bash
  docker pull redis:latest
  docker pull mysql:latest
  ```

### 2. 端口冲突

如果端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "6379:6379"  # 改为 "6380:6379" 等
```

或者停止占用端口的服务：
```bash
# 查找占用端口的进程
lsof -i :6379
lsof -i :3306

# 停止占用端口的容器
docker ps --filter "publish=6379" -q | xargs docker stop
```

### 3. 查看日志

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs mysql
docker-compose logs redis
```

### 4. 重启服务

```bash
# 重启特定服务
docker-compose restart mysql
```

### 5. 完全重置

```bash
# 停止并删除所有容器和数据卷
docker-compose down -v

# 重新启动
docker-compose up -d
```

### 6. 容器启动后立即退出

检查容器日志找出原因：
```bash
docker logs <container_name>
docker ps -a  # 查看所有容器状态
```

## 开发建议

1. **开发环境**：使用 Docker Compose 快速启动所有依赖服务
2. **测试环境**：可以快速重置环境，保证测试一致性
3. **团队协作**：团队成员无需手动安装各种服务，直接运行 `docker-compose up -d` 即可

## 注意事项

1. 确保已安装 Docker 和 Docker Compose
2. 首次启动 MySQL 时会自动执行 `schema.sql` 创建表结构
3. RocketMQ Console 需要等待 NameServer 和 Broker 启动后才能访问
4. 数据卷会持久化数据，删除容器不会丢失数据（除非使用 `-v` 参数）

## 与项目模块的对应关系

| Docker服务 | 项目模块 | 用途 |
|-----------|---------|------|
| redis | redis-example | Redis客户端示例 |
| mysql | mysql-example | MySQL数据库示例 |
| rocketmq-nameserver | mq-rocketmq-example | RocketMQ NameServer |
| rocketmq-broker | mq-rocketmq-example | RocketMQ Broker |
| rocketmq-console | - | RocketMQ管理控制台 |

## 更新配置

如果修改了 `docker-compose.yml`，需要重新创建容器：

```bash
docker-compose up -d --force-recreate
```

