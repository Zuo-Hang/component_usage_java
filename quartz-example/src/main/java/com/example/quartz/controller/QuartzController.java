package com.example.quartz.controller;

import com.example.quartz.job.DataProcessingJob;
import com.example.quartz.job.ReportGenerationJob;
import com.example.quartz.job.SimpleJob;
import com.example.quartz.service.QuartzJobService;
import com.example.quartz.service.QuartzMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Quartz任务调度控制器
 * 
 * 提供REST API接口：
 * 1. 任务管理（添加/删除/暂停/恢复）
 * 2. 任务查询（状态/列表）
 * 3. 任务监控（执行历史/集群节点）
 */
@Slf4j
@RestController
@RequestMapping("/quartz")
public class QuartzController {

    @Autowired
    private QuartzJobService jobService;

    @Autowired
    private QuartzMonitorService monitorService;

    // ========== 任务管理接口 ==========

    /**
     * 添加Cron任务
     * POST /quartz/job/cron
     */
    @PostMapping("/job/cron")
    public Map<String, Object> addCronJob(
            @RequestParam String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup,
            @RequestParam String jobClass,
            @RequestParam String cronExpression,
            @RequestParam(required = false) Map<String, Object> jobData) {
        
        Class<? extends org.quartz.Job> jobClassObj;
        try {
            switch (jobClass) {
                case "SimpleJob":
                    jobClassObj = SimpleJob.class;
                    break;
                case "DataProcessingJob":
                    jobClassObj = DataProcessingJob.class;
                    break;
                case "ReportGenerationJob":
                    jobClassObj = ReportGenerationJob.class;
                    break;
                default:
                    throw new IllegalArgumentException("不支持的任务类: " + jobClass);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "任务类不存在: " + e.getMessage());
            return response;
        }
        
        boolean success = jobService.addCronJob(jobName, jobGroup, jobClassObj, cronExpression, jobData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("cronExpression", cronExpression);
        response.put("message", success ? "添加Cron任务成功" : "添加Cron任务失败");
        return response;
    }

    /**
     * 添加Simple任务
     * POST /quartz/job/simple
     */
    @PostMapping("/job/simple")
    public Map<String, Object> addSimpleJob(
            @RequestParam String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup,
            @RequestParam String jobClass,
            @RequestParam(defaultValue = "0") long startDelay,
            @RequestParam long repeatInterval,
            @RequestParam(defaultValue = "-1") int repeatCount,
            @RequestParam(required = false) Map<String, Object> jobData) {
        
        Class<? extends org.quartz.Job> jobClassObj;
        try {
            switch (jobClass) {
                case "SimpleJob":
                    jobClassObj = SimpleJob.class;
                    break;
                case "DataProcessingJob":
                    jobClassObj = DataProcessingJob.class;
                    break;
                case "ReportGenerationJob":
                    jobClassObj = ReportGenerationJob.class;
                    break;
                default:
                    throw new IllegalArgumentException("不支持的任务类: " + jobClass);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "任务类不存在: " + e.getMessage());
            return response;
        }
        
        Date startTime = new Date(System.currentTimeMillis() + startDelay);
        boolean success = jobService.addSimpleJob(jobName, jobGroup, jobClassObj, 
                startTime, repeatInterval, repeatCount, jobData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("repeatInterval", repeatInterval);
        response.put("repeatCount", repeatCount);
        response.put("message", success ? "添加Simple任务成功" : "添加Simple任务失败");
        return response;
    }

    /**
     * 删除任务
     * DELETE /quartz/job/{jobName}?jobGroup=DEFAULT
     */
    @DeleteMapping("/job/{jobName}")
    public Map<String, Object> deleteJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        boolean success = jobService.deleteJob(jobName, jobGroup);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("message", success ? "删除任务成功" : "删除任务失败");
        return response;
    }

    /**
     * 暂停任务
     * PUT /quartz/job/{jobName}/pause?jobGroup=DEFAULT
     */
    @PutMapping("/job/{jobName}/pause")
    public Map<String, Object> pauseJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        boolean success = jobService.pauseJob(jobName, jobGroup);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("message", success ? "暂停任务成功" : "暂停任务失败");
        return response;
    }

    /**
     * 恢复任务
     * PUT /quartz/job/{jobName}/resume?jobGroup=DEFAULT
     */
    @PutMapping("/job/{jobName}/resume")
    public Map<String, Object> resumeJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        boolean success = jobService.resumeJob(jobName, jobGroup);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("message", success ? "恢复任务成功" : "恢复任务失败");
        return response;
    }

    /**
     * 立即执行任务
     * POST /quartz/job/{jobName}/trigger?jobGroup=DEFAULT
     */
    @PostMapping("/job/{jobName}/trigger")
    public Map<String, Object> triggerJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        boolean success = jobService.triggerJob(jobName, jobGroup);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("message", success ? "立即执行任务成功" : "立即执行任务失败");
        return response;
    }

    /**
     * 更新Cron表达式
     * PUT /quartz/job/{jobName}/cron?cronExpression=0 0/5 * * * ?&jobGroup=DEFAULT
     */
    @PutMapping("/job/{jobName}/cron")
    public Map<String, Object> updateCronExpression(
            @PathVariable String jobName,
            @RequestParam String cronExpression,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        boolean success = jobService.updateCronExpression(jobName, jobGroup, cronExpression);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("jobName", jobName);
        response.put("jobGroup", jobGroup);
        response.put("cronExpression", cronExpression);
        response.put("message", success ? "更新Cron表达式成功" : "更新Cron表达式失败");
        return response;
    }

    // ========== 任务查询接口 ==========

    /**
     * 获取任务状态
     * GET /quartz/job/{jobName}?jobGroup=DEFAULT
     */
    @GetMapping("/job/{jobName}")
    public Map<String, Object> getJobStatus(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "DEFAULT") String jobGroup) {
        
        return jobService.getJobStatus(jobName, jobGroup);
    }

    /**
     * 获取所有任务
     * GET /quartz/jobs
     */
    @GetMapping("/jobs")
    public Map<String, Object> getAllJobs() {
        List<Map<String, Object>> jobs = jobService.getAllJobs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobs", jobs);
        response.put("count", jobs.size());
        return response;
    }

    // ========== 任务监控接口 ==========

    /**
     * 获取Scheduler状态
     * GET /quartz/scheduler/status
     */
    @GetMapping("/scheduler/status")
    public Map<String, Object> getSchedulerStatus() {
        return monitorService.getSchedulerStatus();
    }

    /**
     * 获取正在执行的任务
     * GET /quartz/jobs/executing
     */
    @GetMapping("/jobs/executing")
    public Map<String, Object> getCurrentlyExecutingJobs() {
        List<Map<String, Object>> jobs = monitorService.getCurrentlyExecutingJobs();
        
        Map<String, Object> response = new HashMap<>();
        response.put("executingJobs", jobs);
        response.put("count", jobs.size());
        return response;
    }

    /**
     * 获取任务执行历史
     * GET /quartz/jobs/history?jobName=xxx&jobGroup=DEFAULT&limit=100
     */
    @GetMapping("/jobs/history")
    public Map<String, Object> getJobExecutionHistory(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobGroup,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<Map<String, Object>> history = monitorService.getJobExecutionHistory(jobName, jobGroup, limit);
        
        Map<String, Object> response = new HashMap<>();
        response.put("history", history);
        response.put("count", history.size());
        return response;
    }

    /**
     * 获取集群节点信息
     * GET /quartz/cluster/nodes
     */
    @GetMapping("/cluster/nodes")
    public Map<String, Object> getClusterNodes() {
        List<Map<String, Object>> nodes = monitorService.getClusterNodes();
        
        Map<String, Object> response = new HashMap<>();
        response.put("nodes", nodes);
        response.put("count", nodes.size());
        return response;
    }

    /**
     * 获取任务统计信息
     * GET /quartz/statistics
     */
    @GetMapping("/statistics")
    public Map<String, Object> getJobStatistics() {
        return monitorService.getJobStatistics();
    }

    // ========== 批量操作接口 ==========

    /**
     * 暂停所有任务
     * PUT /quartz/jobs/pause-all
     */
    @PutMapping("/jobs/pause-all")
    public Map<String, Object> pauseAll() {
        boolean success = jobService.pauseAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "暂停所有任务成功" : "暂停所有任务失败");
        return response;
    }

    /**
     * 恢复所有任务
     * PUT /quartz/jobs/resume-all
     */
    @PutMapping("/jobs/resume-all")
    public Map<String, Object> resumeAll() {
        boolean success = jobService.resumeAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "恢复所有任务成功" : "恢复所有任务失败");
        return response;
    }

    /**
     * 健康检查
     * GET /quartz/health
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "quartz-example");
        return response;
    }
}
