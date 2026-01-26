# Quartz 示例模块

本模块演示如何在生产环境中使用 Quartz 任务调度框架，包含数据库持久化、集群模式、动态任务管理等核心功能。

## ⚠️ 重要说明

**Quartz 是任务调度框架，主要用于：**
- 定时任务调度
- 复杂时间表达式（Cron）
- 任务持久化
- 集群模式（多实例部署）

### 适用场景 ✅
- 定时报表生成
- 数据同步任务
- 定时数据清理
- 定时通知发送
- 需要持久化的任务调度

### 不适用场景 ❌
- 简单的延迟任务（使用 Spring @Scheduled 更合适）
- 不需要持久化的轻量级任务

## 生产环境核心特性

### 1. 数据库持久化（JobStore）

- ✅ 任务信息持久化到数据库
- ✅ 应用重启后任务自动恢复
- ✅ 支持任务历史记录查询

### 2. 集群模式

- ✅ 多实例部署，避免重复执行
- ✅ 自动故障转移
- ✅ 负载均衡

### 3. 动态任务管理

- ✅ 运行时添加/删除任务
- ✅ 暂停/恢复任务
- ✅ 更新Cron表达式
- ✅ 立即执行任务

### 4. 任务监控

- ✅ 任务执行状态查询
- ✅ 任务执行历史记录
- ✅ 集群节点信息
- ✅ 正在执行的任务

### 5. 异常处理和重试

- ✅ 任务执行异常处理
- ✅ 自动重试机制
- ✅ 失败任务记录

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS quartz_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化Quartz表结构

执行 `src/main/resources/schema-quartz.sql` 创建Quartz表结构。

**注意**：Quartz也可以自动创建表，但建议手动创建以便更好地控制。

### 3. 启动应用

```bash
cd quartz-example
mvn spring-boot:run
```

### 4. 添加Cron任务

```bash
curl -X POST "http://localhost:8091/quartz/job/cron?jobName=test-job&jobClass=SimpleJob&cronExpression=0/5 * * * * ?"
```

### 5. 查询任务状态

```bash
curl http://localhost:8091/quartz/job/test-job
```

## API 接口

### 任务管理

#### 1. 添加Cron任务
```bash
POST /quartz/job/cron
参数：
- jobName: 任务名称
- jobGroup: 任务组（默认：DEFAULT）
- jobClass: 任务类（SimpleJob/DataProcessingJob/ReportGenerationJob）
- cronExpression: Cron表达式
- jobData: 任务参数（可选，JSON格式）
```

**Cron表达式示例**：
- `0 0/5 * * * ?` - 每5分钟执行一次
- `0 0 12 * * ?` - 每天12点执行
- `0 0 0 1 * ?` - 每月1号执行
- `0 0 0 ? * MON` - 每周一执行

#### 2. 添加Simple任务
```bash
POST /quartz/job/simple
参数：
- jobName: 任务名称
- jobGroup: 任务组（默认：DEFAULT）
- jobClass: 任务类
- startDelay: 延迟启动时间（毫秒，默认：0）
- repeatInterval: 重复间隔（毫秒）
- repeatCount: 重复次数（-1表示无限重复）
- jobData: 任务参数（可选）
```

#### 3. 删除任务
```bash
DELETE /quartz/job/{jobName}?jobGroup=DEFAULT
```

#### 4. 暂停任务
```bash
PUT /quartz/job/{jobName}/pause?jobGroup=DEFAULT
```

#### 5. 恢复任务
```bash
PUT /quartz/job/{jobName}/resume?jobGroup=DEFAULT
```

#### 6. 立即执行任务
```bash
POST /quartz/job/{jobName}/trigger?jobGroup=DEFAULT
```

#### 7. 更新Cron表达式
```bash
PUT /quartz/job/{jobName}/cron?cronExpression=0 0/10 * * * ?&jobGroup=DEFAULT
```

### 任务查询

#### 1. 获取任务状态
```bash
GET /quartz/job/{jobName}?jobGroup=DEFAULT
```

**响应示例**：
```json
{
  "exists": true,
  "jobName": "test-job",
  "jobGroup": "DEFAULT",
  "jobClass": "com.example.quartz.job.SimpleJob",
  "triggerState": "NORMAL",
  "nextFireTime": 1704067200000,
  "previousFireTime": 1704066900000,
  "cronExpression": "0/5 * * * * ?"
}
```

#### 2. 获取所有任务
```bash
GET /quartz/jobs
```

### 任务监控

#### 1. 获取Scheduler状态
```bash
GET /quartz/scheduler/status
```

#### 2. 获取正在执行的任务
```bash
GET /quartz/jobs/executing
```

#### 3. 获取任务执行历史
```bash
GET /quartz/jobs/history?jobName=xxx&jobGroup=DEFAULT&limit=100
```

#### 4. 获取集群节点信息
```bash
GET /quartz/cluster/nodes
```

#### 5. 获取任务统计信息
```bash
GET /quartz/statistics
```

### 批量操作

#### 1. 暂停所有任务
```bash
PUT /quartz/jobs/pause-all
```

#### 2. 恢复所有任务
```bash
PUT /quartz/jobs/resume-all
```

## 配置说明

### application.yml 配置项

```yaml
quartz:
  # Scheduler实例ID
  # AUTO: 自动生成（推荐用于集群模式）
  instanceId: AUTO
  
  # 是否启用集群模式
  clustered: true
  
  # 线程池配置
  threadPool:
    threadCount: 10
```

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quartz_db
    username: root
    password: root
```

## 生产环境最佳实践

### 1. 数据库持久化

#### 使用JobStoreTX
```java
properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
properties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
```

**优势**：
- 任务信息持久化
- 应用重启后任务自动恢复
- 支持任务历史记录

### 2. 集群模式

#### 启用集群模式
```yaml
quartz:
  clustered: true
  instanceId: AUTO  # 自动生成实例ID
```

**配置要点**：
```java
properties.put("org.quartz.jobStore.isClustered", "true");
properties.put("org.quartz.jobStore.clusterCheckinInterval", "20000"); // 20秒
```

**优势**：
- 多实例部署，避免重复执行
- 自动故障转移
- 负载均衡

### 3. 动态任务管理

#### 运行时添加任务
```java
jobService.addCronJob("report-job", "DEFAULT", ReportGenerationJob.class, 
        "0 0 12 * * ?", jobDataMap);
```

#### 更新Cron表达式
```java
jobService.updateCronExpression("report-job", "DEFAULT", "0 0 18 * * ?");
```

**优势**：
- 无需重启应用
- 灵活的任务管理
- 支持热更新

### 4. 异常处理和重试

#### 任务重试机制
```java
public class DataProcessingJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            processData();
        } catch (Exception e) {
            int retryCount = context.getJobDetail().getJobDataMap().getInt("retryCount");
            int maxRetries = context.getJobDetail().getJobDataMap().getInt("maxRetries");
            
            if (retryCount < maxRetries) {
                context.getJobDetail().getJobDataMap().put("retryCount", retryCount + 1);
                JobExecutionException jobException = new JobExecutionException(e);
                jobException.setRefireImmediately(true); // 立即重试
                throw jobException;
            }
        }
    }
}
```

### 5. Misfire策略

#### 配置Misfire策略
```java
CronScheduleBuilder.cronSchedule(cronExpression)
    .withMisfireHandlingInstructionDoNothing()  // 忽略错过的任务
    // 或
    .withMisfireHandlingInstructionFireAndProceed()  // 立即执行错过的任务
```

**策略说明**：
- `DoNothing`: 忽略错过的任务
- `FireAndProceed`: 立即执行错过的任务，然后按正常调度
- `IgnoreMisfires`: 忽略所有错过的任务，按正常调度继续

### 6. 线程池配置

#### 根据任务数量调整
```yaml
quartz:
  threadPool:
    threadCount: 10  # 根据任务数量调整
```

**建议**：
- 任务数量 < 10: threadCount = 5
- 任务数量 10-50: threadCount = 10
- 任务数量 > 50: threadCount = 20

### 7. 任务监控

#### 查询执行历史
```java
List<Map<String, Object>> history = monitorService.getJobExecutionHistory(
    "report-job", "DEFAULT", 100);
```

#### 查询集群节点
```java
List<Map<String, Object>> nodes = monitorService.getClusterNodes();
```

## 典型使用场景

### 1. 定时报表生成

```java
// 每天12点生成报表
jobService.addCronJob("daily-report", "REPORTS", 
        ReportGenerationJob.class, 
        "0 0 12 * * ?",
        Map.of("reportType", "daily"));
```

### 2. 数据同步任务

```java
// 每5分钟同步一次数据
jobService.addCronJob("data-sync", "SYNC", 
        DataProcessingJob.class, 
        "0 0/5 * * * ?",
        Map.of("dataSource", "external-api", 
               "maxRetries", 3, 
               "retryCount", 0));
```

### 3. 数据清理任务

```java
// 每天凌晨2点清理过期数据
jobService.addCronJob("data-cleanup", "CLEANUP", 
        SimpleJob.class, 
        "0 0 2 * * ?",
        Map.of("message", "清理过期数据"));
```

### 4. 集群模式部署

```yaml
# 实例1
quartz:
  instanceId: AUTO
  clustered: true

# 实例2（相同配置）
quartz:
  instanceId: AUTO
  clustered: true
```

**效果**：
- 两个实例共享任务
- 任务只在一个实例上执行
- 实例故障时自动转移

## 常见问题

### Q1: Quartz 和 Spring @Scheduled 如何选择？

**A**: 
- **选择 Quartz**：需要持久化、集群模式、动态任务管理、复杂调度
- **选择 @Scheduled**：简单定时任务、不需要持久化、单机部署

### Q2: 集群模式下如何避免重复执行？

**A**: 
- 启用集群模式（`clustered: true`）
- 使用数据库持久化（JobStoreTX）
- Quartz会自动协调，确保任务只在一个实例上执行

### Q3: 如何实现任务重试？

**A**: 
```java
// 在Job中实现重试逻辑
int retryCount = context.getJobDetail().getJobDataMap().getInt("retryCount");
if (retryCount < maxRetries) {
    context.getJobDetail().getJobDataMap().put("retryCount", retryCount + 1);
    JobExecutionException jobException = new JobExecutionException(e);
    jobException.setRefireImmediately(true);
    throw jobException;
}
```

### Q4: 如何查询任务执行历史？

**A**: 
```java
// 查询执行历史
List<Map<String, Object>> history = monitorService.getJobExecutionHistory(
    "jobName", "jobGroup", 100);
```

### Q5: Misfire 是什么？

**A**: 
- Misfire：任务应该执行但错过了执行时间
- 常见原因：应用关闭、线程池满、系统负载高
- 策略：DoNothing（忽略）、FireAndProceed（立即执行）

### Q6: 如何动态更新Cron表达式？

**A**: 
```java
jobService.updateCronExpression("jobName", "jobGroup", "0 0/10 * * * ?");
```

## 参考资源

- [Quartz 官方文档](https://www.quartz-scheduler.org/documentation/)
- [Quartz Cron表达式](https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html)
- [Spring Boot Quartz集成](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.quartz)
