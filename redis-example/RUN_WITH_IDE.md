# 使用 IDE 运行 Redis 模块（推荐）

## 问题说明

如果遇到 Maven 编译错误（JDK 版本兼容性问题），建议使用 IDE 直接运行，IDE 会自动处理编译问题。

## IntelliJ IDEA

### 1. 打开项目

1. File → Open → 选择项目根目录
2. 等待 Maven 导入完成

### 2. 配置 JDK

1. File → Project Structure → Project
2. Project SDK: 选择 JDK 1.8 或更高版本
3. Project language level: 8 - Lambdas, type annotations etc.

### 3. 运行应用

1. 找到 `redis-example/src/main/java/com/example/redis/RedisExampleApplication.java`
2. 右键 → Run 'RedisExampleApplication'
3. 等待应用启动

### 4. 查看启动日志

控制台应该显示：
```
Started RedisExampleApplication in X.XXX seconds
```

## Eclipse

### 1. 导入项目

1. File → Import → Maven → Existing Maven Projects
2. 选择项目根目录
3. 点击 Finish

### 2. 运行应用

1. 找到 `RedisExampleApplication.java`
2. 右键 → Run As → Java Application
3. 等待应用启动

## VS Code

### 1. 安装扩展

- Java Extension Pack
- Spring Boot Extension Pack

### 2. 运行应用

1. 打开 `RedisExampleApplication.java`
2. 点击类名旁边的运行按钮 ▶️
3. 或按 F5 启动调试

## 验证启动

应用启动后，访问：
- http://localhost:8080/redis/client/jedis/string

如果返回结果，说明启动成功！

## 如果 IDE 也有问题

### 方案1：使用 JDK 8

如果系统有多个 JDK 版本，切换到 JDK 8：

```bash
# macOS 使用 jenv 管理 JDK
jenv local 1.8

# 或设置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
```

### 方案2：升级项目到 JDK 11+

修改 `pom.xml`：

```xml
<java.version>11</java.version>
<maven.compiler.source>11</maven.compiler.source>
<maven.compiler.target>11</maven.compiler.target>
```

### 方案3：使用 Docker 运行

如果本地环境有问题，可以使用 Docker 运行：

```bash
# 构建镜像
docker build -t redis-example .

# 运行容器
docker run -p 8080:8080 redis-example
```
