package com.example.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 简单任务示例
 * 
 * 实现Job接口，定义任务执行逻辑
 */
@Slf4j
public class SimpleJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        
        log.info("执行简单任务: jobName={}, jobGroup={}, fireTime={}", 
                jobName, jobGroup, context.getFireTime());
        
        // 获取任务参数
        String message = context.getJobDetail().getJobDataMap().getString("message");
        if (message != null) {
            log.info("任务参数: message={}", message);
        }
        
        // 模拟业务逻辑
        try {
            Thread.sleep(1000);
            log.info("任务执行完成: jobName={}", jobName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobExecutionException("任务执行被中断", e);
        }
    }
}
