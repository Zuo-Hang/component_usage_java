-- ============================================
-- MySQL示例模块 - 数据库表结构
-- ============================================
-- 说明：此文件包含mysql-example模块使用的所有表结构
-- 支持JDBC Template、MyBatis、MyBatis Plus三种框架的示例

-- ============================================
-- 1. 用户表 (user)
-- ============================================
-- 用途：用于演示三种数据库框架的CRUD操作
-- 支持：JDBC Template、MyBatis、MyBatis Plus

DROP TABLE IF EXISTS `user`;

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

-- ============================================
-- 2. 初始化数据（可选）
-- ============================================
-- 如果需要测试数据，可以取消下面的注释

-- INSERT INTO `user` (`username`, `email`, `age`) VALUES
-- ('admin', 'admin@example.com', 30),
-- ('test_user', 'test@example.com', 25),
-- ('demo_user', 'demo@example.com', 28);

-- ============================================
-- 3. 表结构说明
-- ============================================
-- id: 主键，自增，用于三种框架的ID查询示例
-- username: 用户名，唯一索引，用于条件查询示例
-- email: 邮箱，普通索引，用于条件查询示例
-- age: 年龄，普通索引，用于条件查询和统计示例
-- create_time: 创建时间，自动填充，用于MyBatis Plus字段填充示例
-- update_time: 更新时间，自动更新，用于MyBatis Plus字段填充示例

-- ============================================
-- 4. 使用说明
-- ============================================
-- 1. 执行此SQL文件创建表结构
-- 2. 确保application.yml中的数据库配置正确
-- 3. 三种框架（JDBC Template、MyBatis、MyBatis Plus）共用此表
-- 4. MyBatis Plus会自动填充create_time和update_time字段

