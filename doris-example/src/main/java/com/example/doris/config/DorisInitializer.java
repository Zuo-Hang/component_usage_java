package com.example.doris.config;

import com.example.doris.service.DorisTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Doris初始化器
 * 应用启动时自动创建表结构
 */
@Slf4j
@Component
public class DorisInitializer implements CommandLineRunner {

    @Autowired
    private DorisTableService tableService;

    @Override
    public void run(String... args) {
        try {
            // 创建用户表
            if (!tableService.tableExists("users")) {
                log.info("检测到 users 表不存在，开始创建...");
                tableService.createUserTable();
                log.info("users 表创建成功");
            } else {
                log.info("users 表已存在，跳过创建");
            }

            // 创建订单表
            if (!tableService.tableExists("orders")) {
                log.info("检测到 orders 表不存在，开始创建...");
                tableService.createOrderTable();
                log.info("orders 表创建成功");
            } else {
                log.info("orders 表已存在，跳过创建");
            }

            // 创建订单汇总表（可选）
            // if (!tableService.tableExists("order_summary")) {
            //     tableService.createOrderSummaryTable();
            // }

            // 物化视图需要手动创建（因为依赖表结构）
            // 用户可以通过API手动创建：POST /doris/tables/materialized-view

        } catch (Exception e) {
            log.error("Doris初始化失败", e);
            // 不抛出异常，允许应用继续启动
            // 用户可以通过API手动创建表
        }
    }
}
