package com.example.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Quartz监控服务
 * 
 * 生产环境核心功能：
 * 1. 查询任务执行历史
 * 2. 查询正在执行的任务
 * 3. 查询Scheduler状态
 * 4. 查询集群节点信息
 */
@Slf4j
@Service
public class QuartzMonitorService {

    @Autowired
    private Scheduler scheduler;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取Scheduler状态
     * 
     * @return Scheduler状态信息
     */
    public Map<String, Object> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("schedulerName", scheduler.getSchedulerName());
            status.put("schedulerInstanceId", scheduler.getSchedulerInstanceId());
            status.put("isStarted", scheduler.isStarted());
            status.put("isInStandbyMode", scheduler.isInStandbyMode());
            status.put("isShutdown", scheduler.isShutdown());
            
            // 获取任务统计
            List<String> jobGroupNames = scheduler.getJobGroupNames();
            int totalJobs = 0;
            for (String groupName : jobGroupNames) {
                totalJobs += scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).size();
            }
            status.put("totalJobs", totalJobs);
            status.put("jobGroups", jobGroupNames.size());
            
        } catch (Exception e) {
            log.error("获取Scheduler状态失败", e);
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 获取正在执行的任务
     * 
     * @return 正在执行的任务列表
     */
    public List<Map<String, Object>> getCurrentlyExecutingJobs() {
        List<Map<String, Object>> executingJobs = new ArrayList<>();
        
        try {
            List<JobExecutionContext> contexts = scheduler.getCurrentlyExecutingJobs();
            
            for (JobExecutionContext context : contexts) {
                Map<String, Object> jobInfo = new HashMap<>();
                jobInfo.put("jobName", context.getJobDetail().getKey().getName());
                jobInfo.put("jobGroup", context.getJobDetail().getKey().getGroup());
                jobInfo.put("fireTime", context.getFireTime());
                jobInfo.put("scheduledFireTime", context.getScheduledFireTime());
                jobInfo.put("jobRunTime", context.getJobRunTime());
                jobInfo.put("refireCount", context.getRefireCount());
                
                executingJobs.add(jobInfo);
            }
            
        } catch (Exception e) {
            log.error("获取正在执行的任务失败", e);
        }
        
        return executingJobs;
    }

    /**
     * 查询任务执行历史（从数据库）
     * 
     * 注意：需要Quartz表结构支持
     * 
     * @param jobName 任务名称（可选）
     * @param jobGroup 任务组（可选）
     * @param limit 限制条数
     * @return 执行历史列表
     */
    public List<Map<String, Object>> getJobExecutionHistory(String jobName, String jobGroup, int limit) {
        List<Map<String, Object>> history = new ArrayList<>();
        
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，无法查询执行历史");
            return history;
        }
        
        try {
            // 查询QRTZ_FIRED_TRIGGERS表（Quartz执行历史）
            String sql = """
                SELECT 
                    INSTANCE_NAME,
                    TRIGGER_NAME,
                    TRIGGER_GROUP,
                    JOB_NAME,
                    JOB_GROUP,
                    FIRED_TIME,
                    STATE,
                    JOB_DATA
                FROM QRTZ_FIRED_TRIGGERS
                WHERE 1=1
                """;
            
            List<Object> params = new ArrayList<>();
            
            if (jobName != null && !jobName.isEmpty()) {
                sql += " AND JOB_NAME = ?";
                params.add(jobName);
            }
            
            if (jobGroup != null && !jobGroup.isEmpty()) {
                sql += " AND JOB_GROUP = ?";
                params.add(jobGroup);
            }
            
            sql += " ORDER BY FIRED_TIME DESC LIMIT ?";
            params.add(limit);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
            
            for (Map<String, Object> row : results) {
                Map<String, Object> historyItem = new HashMap<>();
                historyItem.put("instanceName", row.get("INSTANCE_NAME"));
                historyItem.put("triggerName", row.get("TRIGGER_NAME"));
                historyItem.put("triggerGroup", row.get("TRIGGER_GROUP"));
                historyItem.put("jobName", row.get("JOB_NAME"));
                historyItem.put("jobGroup", row.get("JOB_GROUP"));
                historyItem.put("firedTime", row.get("FIRED_TIME"));
                historyItem.put("state", row.get("STATE"));
                
                history.add(historyItem);
            }
            
        } catch (Exception e) {
            log.error("查询任务执行历史失败", e);
        }
        
        return history;
    }

    /**
     * 获取集群节点信息（如果启用集群模式）
     * 
     * @return 集群节点列表
     */
    public List<Map<String, Object>> getClusterNodes() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，无法查询集群节点");
            return nodes;
        }
        
        try {
            // 查询QRTZ_SCHEDULER_STATE表（集群节点信息）
            String sql = """
                SELECT 
                    INSTANCE_NAME,
                    LAST_CHECKIN_TIME,
                    CHECKIN_INTERVAL
                FROM QRTZ_SCHEDULER_STATE
                ORDER BY LAST_CHECKIN_TIME DESC
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            for (Map<String, Object> row : results) {
                Map<String, Object> node = new HashMap<>();
                node.put("instanceName", row.get("INSTANCE_NAME"));
                node.put("lastCheckinTime", row.get("LAST_CHECKIN_TIME"));
                node.put("checkinInterval", row.get("CHECKIN_INTERVAL"));
                
                nodes.add(node);
            }
            
        } catch (Exception e) {
            log.error("查询集群节点失败", e);
        }
        
        return nodes;
    }

    /**
     * 获取任务统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            int totalJobs = 0;
            int pausedJobs = 0;
            int normalJobs = 0;
            
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    totalJobs++;
                    
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    if (!triggers.isEmpty()) {
                        TriggerState state = scheduler.getTriggerState(triggers.get(0).getKey());
                        if (state == TriggerState.PAUSED) {
                            pausedJobs++;
                        } else {
                            normalJobs++;
                        }
                    }
                }
            }
            
            statistics.put("totalJobs", totalJobs);
            statistics.put("pausedJobs", pausedJobs);
            statistics.put("normalJobs", normalJobs);
            statistics.put("currentlyExecuting", scheduler.getCurrentlyExecutingJobs().size());
            
        } catch (Exception e) {
            log.error("获取任务统计失败", e);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }
}
