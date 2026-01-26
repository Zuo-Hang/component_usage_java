package com.example.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Quartz任务调度服务
 * 
 * 生产环境核心功能：
 * 1. 动态添加任务（Cron/Simple Trigger）
 * 2. 删除任务
 * 3. 暂停/恢复任务
 * 4. 立即执行任务
 * 5. 查询任务状态
 * 6. 获取所有任务
 */
@Slf4j
@Service
public class QuartzJobService {

    @Autowired
    private Scheduler scheduler;

    /**
     * 添加Cron任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param jobClass 任务类
     * @param cronExpression Cron表达式
     * @param jobDataMap 任务参数
     * @return 是否成功
     */
    public boolean addCronJob(String jobName, String jobGroup, 
                              Class<? extends Job> jobClass,
                              String cronExpression, 
                              Map<String, Object> jobDataMap) {
        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup)
                    .withDescription("Cron任务: " + jobName)
                    .storeDurably(false) // 非持久化（如果Trigger被删除，Job也会被删除）
                    .build();
            
            // 设置任务参数
            if (jobDataMap != null && !jobDataMap.isEmpty()) {
                jobDetail.getJobDataMap().putAll(jobDataMap);
            }
            
            // 创建CronTrigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_trigger", jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing()) // Misfire策略：忽略
                    .build();
            
            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);
            
            log.info("添加Cron任务成功: jobName={}, jobGroup={}, cronExpression={}", 
                    jobName, jobGroup, cronExpression);
            return true;
        } catch (Exception e) {
            log.error("添加Cron任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 添加简单任务（SimpleTrigger）
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param jobClass 任务类
     * @param startTime 开始时间
     * @param repeatInterval 重复间隔（毫秒）
     * @param repeatCount 重复次数（-1表示无限重复）
     * @param jobDataMap 任务参数
     * @return 是否成功
     */
    public boolean addSimpleJob(String jobName, String jobGroup,
                                Class<? extends Job> jobClass,
                                Date startTime,
                                long repeatInterval,
                                int repeatCount,
                                Map<String, Object> jobDataMap) {
        try {
            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup)
                    .withDescription("Simple任务: " + jobName)
                    .storeDurably(false)
                    .build();
            
            // 设置任务参数
            if (jobDataMap != null && !jobDataMap.isEmpty()) {
                jobDetail.getJobDataMap().putAll(jobDataMap);
            }
            
            // 创建SimpleTrigger
            SimpleTriggerBuilder triggerBuilder = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_trigger", jobGroup)
                    .startAt(startTime != null ? startTime : new Date());
            
            if (repeatCount == -1) {
                // 无限重复
                triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(repeatInterval)
                        .repeatForever()
                        .withMisfireHandlingInstructionNowWithExistingCount());
            } else {
                // 有限次数重复
                triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(repeatInterval)
                        .withRepeatCount(repeatCount)
                        .withMisfireHandlingInstructionNowWithExistingCount());
            }
            
            Trigger trigger = triggerBuilder.build();
            
            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);
            
            log.info("添加Simple任务成功: jobName={}, jobGroup={}, repeatInterval={}ms, repeatCount={}", 
                    jobName, jobGroup, repeatInterval, repeatCount);
            return true;
        } catch (Exception e) {
            log.error("添加Simple任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 删除任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否成功
     */
    public boolean deleteJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            boolean deleted = scheduler.deleteJob(jobKey);
            
            if (deleted) {
                log.info("删除任务成功: jobName={}, jobGroup={}", jobName, jobGroup);
            } else {
                log.warn("任务不存在: jobName={}, jobGroup={}", jobName, jobGroup);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("删除任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 暂停任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否成功
     */
    public boolean pauseJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            
            log.info("暂停任务成功: jobName={}, jobGroup={}", jobName, jobGroup);
            return true;
        } catch (Exception e) {
            log.error("暂停任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 恢复任务
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否成功
     */
    public boolean resumeJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);
            
            log.info("恢复任务成功: jobName={}, jobGroup={}", jobName, jobGroup);
            return true;
        } catch (Exception e) {
            log.error("恢复任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 立即执行任务（触发一次）
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 是否成功
     */
    public boolean triggerJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);
            
            log.info("立即执行任务成功: jobName={}, jobGroup={}", jobName, jobGroup);
            return true;
        } catch (Exception e) {
            log.error("立即执行任务失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 更新Cron表达式
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @param cronExpression 新的Cron表达式
     * @return 是否成功
     */
    public boolean updateCronExpression(String jobName, String jobGroup, String cronExpression) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger", jobGroup);
            
            // 获取现有Trigger
            CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (oldTrigger == null) {
                log.warn("Trigger不存在: jobName={}, jobGroup={}", jobName, jobGroup);
                return false;
            }
            
            // 创建新Trigger
            CronTrigger newTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
            
            // 更新Trigger
            scheduler.rescheduleJob(triggerKey, newTrigger);
            
            log.info("更新Cron表达式成功: jobName={}, jobGroup={}, cronExpression={}", 
                    jobName, jobGroup, cronExpression);
            return true;
        } catch (Exception e) {
            log.error("更新Cron表达式失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            return false;
        }
    }

    /**
     * 获取任务状态
     * 
     * @param jobName 任务名称
     * @param jobGroup 任务组
     * @return 任务状态信息
     */
    public Map<String, Object> getJobStatus(String jobName, String jobGroup) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            
            if (jobDetail == null) {
                status.put("exists", false);
                return status;
            }
            
            status.put("exists", true);
            status.put("jobName", jobName);
            status.put("jobGroup", jobGroup);
            status.put("jobClass", jobDetail.getJobClass().getName());
            status.put("description", jobDetail.getDescription());
            status.put("durable", jobDetail.isDurable());
            
            // 获取Trigger信息
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (!triggers.isEmpty()) {
                Trigger trigger = triggers.get(0);
                status.put("triggerState", scheduler.getTriggerState(trigger.getKey()));
                status.put("nextFireTime", trigger.getNextFireTime());
                status.put("previousFireTime", trigger.getPreviousFireTime());
                
                if (trigger instanceof CronTrigger) {
                    status.put("cronExpression", ((CronTrigger) trigger).getCronExpression());
                }
            }
            
        } catch (Exception e) {
            log.error("获取任务状态失败: jobName={}, jobGroup={}", jobName, jobGroup, e);
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    /**
     * 获取所有任务
     * 
     * @return 任务列表
     */
    public List<Map<String, Object>> getAllJobs() {
        List<Map<String, Object>> jobs = new ArrayList<>();
        
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    Map<String, Object> jobInfo = getJobStatus(jobKey.getName(), jobKey.getGroup());
                    jobs.add(jobInfo);
                }
            }
        } catch (Exception e) {
            log.error("获取所有任务失败", e);
        }
        
        return jobs;
    }

    /**
     * 暂停所有任务
     * 
     * @return 是否成功
     */
    public boolean pauseAll() {
        try {
            scheduler.pauseAll();
            log.info("暂停所有任务成功");
            return true;
        } catch (Exception e) {
            log.error("暂停所有任务失败", e);
            return false;
        }
    }

    /**
     * 恢复所有任务
     * 
     * @return 是否成功
     */
    public boolean resumeAll() {
        try {
            scheduler.resumeAll();
            log.info("恢复所有任务成功");
            return true;
        } catch (Exception e) {
            log.error("恢复所有任务失败", e);
            return false;
        }
    }
}
