package com.example.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 数据处理任务示例
 * 
 * 演示如何处理可能失败的任务，支持重试机制
 */
@Slf4j
public class DataProcessingJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        
        log.info("开始执行数据处理任务: jobName={}", jobName);
        
        try {
            // 模拟数据处理
            processData(context);
            
            log.info("数据处理任务执行成功: jobName={}", jobName);
        } catch (Exception e) {
            log.error("数据处理任务执行失败: jobName={}", jobName, e);
            
            // 获取重试次数
            int retryCount = context.getJobDetail().getJobDataMap().getInt("retryCount");
            int maxRetries = context.getJobDetail().getJobDataMap().getInt("maxRetries");
            
            if (retryCount < maxRetries) {
                // 更新重试次数
                context.getJobDetail().getJobDataMap().put("retryCount", retryCount + 1);
                
                // 重新抛出异常，触发重试
                JobExecutionException jobException = new JobExecutionException(e);
                jobException.setRefireImmediately(true); // 立即重试
                throw jobException;
            } else {
                // 超过最大重试次数，记录失败
                log.error("任务重试次数已达上限: jobName={}, maxRetries={}", jobName, maxRetries);
                throw new JobExecutionException("任务执行失败，已超过最大重试次数", e, false);
            }
        }
    }

    private void processData(JobExecutionContext context) throws Exception {
        // 模拟数据处理逻辑
        String dataSource = context.getJobDetail().getJobDataMap().getString("dataSource");
        log.info("处理数据源: {}", dataSource);
        
        // 模拟可能失败的操作
        if (Math.random() < 0.3) { // 30%概率失败
            throw new RuntimeException("数据处理失败");
        }
        
        Thread.sleep(2000); // 模拟处理时间
    }
}
