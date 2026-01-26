package com.example.clickhouse.config;

import com.example.clickhouse.service.ClickHouseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ClickHouse初始化器
 * 应用启动时自动创建表结构
 */
@Slf4j
@Component
public class ClickHouseInitializer implements CommandLineRunner {

    @Autowired
    private ClickHouseTableService tableService;

    @Override
    public void run(String... args) {
        try {
            // 检查表是否存在，不存在则创建
            if (!tableService.tableExists("user_behavior_log")) {
                log.info("检测到 user_behavior_log 表不存在，开始创建...");
                tableService.createUserBehaviorLogTable();
                log.info("user_behavior_log 表创建成功");
            } else {
                log.info("user_behavior_log 表已存在，跳过创建");
            }

            // 创建物化视图（可选）
            // 注意：物化视图如果已存在会报错，这里不自动创建
            // 用户可以通过API手动创建：POST /clickhouse/tables/materialized-view

        } catch (Exception e) {
            log.error("ClickHouse初始化失败", e);
            // 不抛出异常，允许应用继续启动
            // 用户可以通过API手动创建表
        }
    }
}
