package com.example.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 报表生成任务示例
 * 
 * 演示定时生成报表的场景
 */
@Slf4j
public class ReportGenerationJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        String reportType = context.getJobDetail().getJobDataMap().getString("reportType");
        
        log.info("开始生成报表: jobName={}, reportType={}, fireTime={}", 
                jobName, reportType, context.getFireTime());
        
        try {
            // 生成报表
            String reportPath = generateReport(reportType);
            
            log.info("报表生成成功: jobName={}, reportPath={}", jobName, reportPath);
            
            // 将结果存储到JobDataMap（可选）
            context.getJobDetail().getJobDataMap().put("lastReportPath", reportPath);
            context.getJobDetail().getJobDataMap().put("lastGenerateTime", 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            log.error("报表生成失败: jobName={}", jobName, e);
            throw new JobExecutionException("报表生成失败", e);
        }
    }

    private String generateReport(String reportType) throws Exception {
        // 模拟报表生成
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String reportPath = String.format("/reports/%s_%s.pdf", reportType, timestamp);
        
        log.info("生成报表: type={}, path={}", reportType, reportPath);
        
        // 模拟生成时间
        Thread.sleep(3000);
        
        return reportPath;
    }
}
