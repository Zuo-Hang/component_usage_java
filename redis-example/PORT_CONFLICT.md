# 端口冲突解决方案

## 问题：端口 8080 被占用

如果遇到 `Port 8080 is already in use` 错误，说明端口已被占用。

## 快速解决

### 方法1：停止占用端口的进程（推荐）

```bash
# 查找占用 8080 端口的进程
lsof -i :8080

# 停止进程（替换 PID 为实际进程ID）
kill -9 <PID>

# 或者一键停止
lsof -ti :8080 | xargs kill -9
```

### 方法2：修改应用端口

编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 8081  # 改为其他端口，如 8081
```

然后访问：`http://localhost:8081`

### 方法3：查找并停止 Spring Boot 应用

```bash
# 查找所有 Spring Boot 进程
ps aux | grep "spring-boot:run\|RedisExampleApplication" | grep -v grep

# 停止进程
kill -9 <PID>
```

## 验证端口已释放

```bash
# 检查端口是否已释放
lsof -i :8080

# 如果没有输出，说明端口已释放
```

## 重新启动应用

端口释放后，重新启动：

```bash
cd redis-example
mvn spring-boot:run
```

## 预防措施

1. **启动前检查端口**：
```bash
lsof -i :8080
```

2. **使用不同端口**：如果经常有冲突，可以修改为其他端口（如 8081）

3. **优雅停止应用**：使用 `Ctrl+C` 停止应用，而不是直接关闭终端
