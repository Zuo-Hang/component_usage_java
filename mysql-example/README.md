# MySQL 使用示例模块

本模块演示了三种数据库框架（JDBC Template、MyBatis、MyBatis Plus）的使用方式，帮助开发者理解不同框架的特点和适用场景。

## 功能特性

### 三种框架对比

| 特性 | JDBC Template | MyBatis | MyBatis Plus |
|------|---------------|---------|--------------|
| SQL控制 | 完全控制 | 完全控制 | 自动生成 |
| 学习成本 | 低 | 中 | 低 |
| 开发效率 | 低 | 中 | 高 |
| 灵活性 | 高 | 高 | 中 |
| 推荐场景 | 简单CRUD | 复杂SQL | 标准CRUD |

## 快速开始

### 1. 启动 MySQL

使用 Docker 启动 MySQL：

```bash
docker run -d -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=testdb \
  mysql:latest
```

或使用 Docker Compose（在项目根目录）：

```bash
docker-compose up -d mysql
```

### 2. 初始化数据库

执行建表语句：

```bash
mysql -h localhost -u root -proot testdb < src/main/resources/schema.sql
```

或直接在 MySQL 客户端执行 `src/main/resources/schema.sql` 文件。

### 3. 配置数据库连接

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: root
```

### 4. 运行应用

```bash
cd mysql-example
mvn spring-boot:run
```

应用将在 `http://localhost:8081` 启动。

### 5. 测试接口

```bash
# JDBC Template 示例
curl -X POST "http://localhost:8081/mysql/framework/jdbc/user" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","age":25}'

# MyBatis 示例
curl -X POST "http://localhost:8081/mysql/framework/mybatis/user" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","age":25}'

# MyBatis Plus 示例
curl -X POST "http://localhost:8081/mysql/framework/mybatis-plus/user" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","age":25}'
```

## API 端点

### JDBC Template

- `POST /mysql/framework/jdbc/user` - 创建用户
- `GET /mysql/framework/jdbc/user/{id}` - 根据ID查询
- `GET /mysql/framework/jdbc/users` - 查询所有用户
- `PUT /mysql/framework/jdbc/user/{id}` - 更新用户
- `DELETE /mysql/framework/jdbc/user/{id}` - 删除用户
- `GET /mysql/framework/jdbc/users/age/{age}` - 根据年龄查询
- `GET /mysql/framework/jdbc/count` - 获取用户总数
- `POST /mysql/framework/jdbc/batch` - 批量插入
- `GET /mysql/framework/jdbc/users/conditions` - 复杂条件查询

### MyBatis

- `POST /mysql/framework/mybatis/user` - 创建用户
- `GET /mysql/framework/mybatis/user/{id}` - 根据ID查询
- `GET /mysql/framework/mybatis/users` - 查询所有用户
- `PUT /mysql/framework/mybatis/user/{id}` - 更新用户
- `DELETE /mysql/framework/mybatis/user/{id}` - 删除用户
- `GET /mysql/framework/mybatis/user/username/{username}` - 根据用户名查询

### MyBatis Plus

- `POST /mysql/framework/mybatis-plus/user` - 创建用户
- `GET /mysql/framework/mybatis-plus/user/{id}` - 根据ID查询
- `GET /mysql/framework/mybatis-plus/users` - 查询所有用户
- `PUT /mysql/framework/mybatis-plus/user/{id}` - 更新用户
- `DELETE /mysql/framework/mybatis-plus/user/{id}` - 删除用户
- `GET /mysql/framework/mybatis-plus/users/age/{age}` - 根据年龄查询
- `GET /mysql/framework/mybatis-plus/users/conditions` - 复杂条件查询
- `GET /mysql/framework/mybatis-plus/users/page` - 分页查询
- `GET /mysql/framework/mybatis-plus/users/page/condition` - 分页条件查询
- `POST /mysql/framework/mybatis-plus/batch` - 批量插入
- `PUT /mysql/framework/mybatis-plus/batch/age` - 批量更新年龄
- `GET /mysql/framework/mybatis-plus/count/age/{age}` - 统计查询
- `GET /mysql/framework/mybatis-plus/exists/username/{username}` - 存在性查询

### 框架对比接口

- `GET /mysql/framework/compare/create` - 创建操作对比
- `GET /mysql/framework/compare/query` - 查询操作对比
- `GET /mysql/framework/compare/condition` - 条件查询对比
- `GET /mysql/framework/compare/features` - 功能特性对比

## 配置说明

配置文件：`src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/testdb?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 项目结构

```
mysql-example/
├── src/main/java/com/example/mysql/
│   ├── MySQLExampleApplication.java      # 启动类
│   ├── config/
│   │   └── MyBatisPlusConfig.java        # MyBatis Plus配置
│   ├── controller/
│   │   ├── FrameworkExampleController.java  # 框架对比控制器
│   │   └── MySQLExampleController.java       # MySQL示例控制器
│   ├── mapper/
│   │   ├── UserMapper.java               # MyBatis Mapper
│   │   └── UserPlusMapper.java           # MyBatis Plus Mapper
│   ├── model/
│   │   ├── User.java                     # MyBatis实体类
│   │   └── UserPlus.java                 # MyBatis Plus实体类
│   └── service/
│       ├── JdbcTemplateExampleService.java   # JDBC Template服务
│       ├── MyBatisExampleService.java         # MyBatis服务
│       └── MyBatisPlusExampleService.java     # MyBatis Plus服务
└── src/main/resources/
    ├── application.yml                    # 配置文件
    ├── schema.sql                        # 建表语句
    └── README_DATABASE.md                # 数据库说明文档
```

## 数据库表结构

详细表结构说明请查看：[README_DATABASE.md](src/main/resources/README_DATABASE.md)

### 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| email | VARCHAR(100) | 邮箱 |
| age | INT | 年龄 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 框架选择建议

### JDBC Template
- ✅ 适合需要完全控制SQL的场景
- ✅ 简单的CRUD操作
- ✅ 学习成本低
- ❌ 需要手写大量SQL
- ❌ 开发效率较低

### MyBatis
- ✅ 适合复杂SQL查询
- ✅ SQL与代码分离，易于维护
- ✅ 支持动态SQL
- ❌ 需要编写Mapper XML文件
- ❌ 简单CRUD也需要写SQL

### MyBatis Plus（推荐）
- ✅ 适合90%的CRUD场景
- ✅ 快速开发，无需手写SQL
- ✅ 内置分页、条件构造器等高级功能
- ✅ 自动填充字段（createTime、updateTime）
- ❌ 复杂SQL仍需手写
- ❌ 学习成本略高

## 学习建议

1. **初学者**：先看 `JdbcTemplateExampleService`，了解基本的数据库操作
2. **进阶**：学习 `MyBatisExampleService`，了解SQL映射和动态SQL
3. **高级**：研究 `MyBatisPlusExampleService`，学习条件构造器和分页功能
4. **实践**：根据实际项目需求选择合适的框架

## 常见问题

### 数据库连接失败

1. 确保 MySQL 服务已启动
2. 检查 `application.yml` 中的数据库配置
3. 确认数据库已创建：`CREATE DATABASE testdb;`

### 表不存在

执行 `src/main/resources/schema.sql` 创建表结构。

### MyBatis Plus 分页不生效

确保已配置 `MyBatisPlusConfig` 中的分页插件。
