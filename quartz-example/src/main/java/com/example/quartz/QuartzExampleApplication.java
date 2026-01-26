package com.example.quartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Quartz示例应用主类
 * 
 * 生产环境核心特性：
 * 1. 数据库持久化（JobStore） - 任务信息持久化到数据库
 * 2. 集群模式 - 多实例部署，避免重复执行
 * 3. 动态任务管理 - 运行时添加/删除/暂停/恢复任务
 * 4. 任务监控 - 任务执行状态和历史记录
 * 5. 异常处理 - 任务执行异常处理和重试
 * 6. Cron表达式管理 - 支持Cron表达式调度
 */
@SpringBootApplication
public class QuartzExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzExampleApplication.class, args);
    }
}
