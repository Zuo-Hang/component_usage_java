# MySQL示例模块 - 数据库说明

## 数据库表结构

### 用户表 (user)

此表用于演示JDBC Template、MyBatis、MyBatis Plus三种数据库框架的使用。

#### 表结构

```sql
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `age` INT(11) DEFAULT NULL COMMENT '年龄',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_age` (`age`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### 字段说明

| 字段名 | 类型 | 说明 | 用途 |
|--------|------|------|------|
| id | BIGINT | 主键，自增 | 三种框架的ID查询示例 |
| username | VARCHAR(50) | 用户名，唯一 | 条件查询、唯一性验证示例 |
| email | VARCHAR(100) | 邮箱 | 条件查询示例 |
| age | INT | 年龄 | 条件查询、统计查询示例 |
| create_time | DATETIME | 创建时间，自动填充 | MyBatis Plus字段填充示例 |
| update_time | DATETIME | 更新时间，自动更新 | MyBatis Plus字段填充示例 |

#### 索引说明

- **PRIMARY KEY (id)**: 主键索引，用于ID查询
- **UNIQUE KEY uk_username (username)**: 唯一索引，确保用户名唯一
- **KEY idx_email (email)**: 普通索引，优化邮箱查询
- **KEY idx_age (age)**: 普通索引，优化年龄查询和统计
- **KEY idx_create_time (create_time)**: 普通索引，优化时间排序查询

## 初始化步骤

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS test_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. 执行建表语句

执行 `src/main/resources/schema.sql` 文件：

```bash
# 方式1：使用MySQL命令行
mysql -u root -p test_db < src/main/resources/schema.sql

# 方式2：在MySQL客户端中执行
source src/main/resources/schema.sql;
```

### 3. 配置数据库连接

在 `application.yml` 中配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

## 三种框架使用说明

### JDBC Template
- 直接使用SQL语句操作此表
- 需要手动处理结果映射
- 示例：`JdbcTemplateExampleService`

### MyBatis
- 使用注解方式编写SQL
- 通过Mapper接口映射SQL
- 示例：`MyBatisExampleService`、`UserMapper`

### MyBatis Plus
- 继承BaseMapper自动获得CRUD方法
- 使用条件构造器进行查询
- 自动填充create_time和update_time
- 示例：`MyBatisPlusExampleService`、`UserPlusMapper`

## 测试数据

如果需要测试数据，可以执行以下SQL：

```sql
INSERT INTO `user` (`username`, `email`, `age`) VALUES
('admin', 'admin@example.com', 30),
('test_user', 'test@example.com', 25),
('demo_user', 'demo@example.com', 28),
('user1', 'user1@example.com', 22),
('user2', 'user2@example.com', 35);
```

## 注意事项

1. **表名**: 三种框架共用同一个`user`表
2. **字段映射**: 
   - JDBC Template和MyBatis使用`User`实体类
   - MyBatis Plus使用`UserPlus`实体类（支持字段自动填充）
3. **时间字段**: 
   - `create_time`和`update_time`在数据库层面有默认值
   - MyBatis Plus会自动填充这些字段（如果配置了MetaObjectHandler）
4. **索引**: 已为常用查询字段创建索引，提升查询性能

## 清理数据

如果需要清空测试数据：

```sql
-- 清空表数据（保留表结构）
TRUNCATE TABLE `user`;

-- 或者删除表
DROP TABLE IF EXISTS `user`;
```

