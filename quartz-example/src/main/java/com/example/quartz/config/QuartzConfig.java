package com.example.quartz.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz配置类
 * 
 * 生产环境最佳实践：
 * 1. 使用数据库持久化（JobStoreTX）
 * 2. 启用集群模式（isClustered=true）
 * 3. 配置线程池大小
 * 4. 设置合理的misfire策略
 * 5. 配置数据源
 */
@Slf4j
@Configuration
public class QuartzConfig {

    @Value("${spring.application.name:quartz-example}")
    private String instanceName;

    @Value("${quartz.instanceId:AUTO}")
    private String instanceId;

    @Value("${quartz.clustered:true}")
    private boolean clustered;

    @Value("${quartz.threadPool.threadCount:10}")
    private int threadCount;

    @org.springframework.beans.factory.annotation.Autowired
    private Environment environment;

    /**
     * 配置Quartz SchedulerFactoryBean
     * 
     * 生产环境配置要点：
     * - 使用数据库持久化（JobStoreTX）
     * - 启用集群模式（多实例部署）
     * - 配置线程池大小
     * - 设置misfire策略
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        
        // 数据源（用于JobStore）
        factory.setDataSource(dataSource);
        
        // Quartz属性配置
        Properties properties = new Properties();
        
        // Scheduler配置
        properties.put("org.quartz.scheduler.instanceName", instanceName);
        properties.put("org.quartz.scheduler.instanceId", instanceId);
        
        // 线程池配置
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", String.valueOf(threadCount));
        properties.put("org.quartz.threadPool.threadPriority", "5");
        
        // JobStore配置（数据库持久化）
        properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.put("org.quartz.jobStore.useProperties", "false");
        properties.put("org.quartz.jobStore.dataSource", "quartzDataSource");
        
        // 集群配置
        if (clustered) {
            properties.put("org.quartz.jobStore.isClustered", "true");
            properties.put("org.quartz.jobStore.clusterCheckinInterval", "20000"); // 20秒
            properties.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
            // Misfire策略：立即执行错过的任务
            properties.put("org.quartz.jobStore.misfireThreshold", "60000"); // 60秒
        }
        
        // 数据源配置（从Environment读取）
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        String datasourceUsername = environment.getProperty("spring.datasource.username");
        String datasourcePassword = environment.getProperty("spring.datasource.password");
        
        properties.put("org.quartz.dataSource.quartzDataSource.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("org.quartz.dataSource.quartzDataSource.URL", datasourceUrl);
        properties.put("org.quartz.dataSource.quartzDataSource.user", datasourceUsername);
        properties.put("org.quartz.dataSource.quartzDataSource.password", datasourcePassword);
        properties.put("org.quartz.dataSource.quartzDataSource.maxConnections", "10");
        properties.put("org.quartz.dataSource.quartzDataSource.validationQuery", "SELECT 1");
        
        factory.setQuartzProperties(properties);
        
        // 应用关闭时等待任务完成
        factory.setWaitForJobsToCompleteOnShutdown(true);
        
        // 覆盖已存在的任务
        factory.setOverwriteExistingJobs(true);
        
        // 延迟启动（等待应用完全启动）
        factory.setStartupDelay(10);
        
        log.info("Quartz Scheduler配置完成: instanceName={}, clustered={}, threadCount={}", 
                instanceName, clustered, threadCount);
        
        return factory;
    }

    /**
     * 获取Scheduler Bean
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        log.info("Quartz Scheduler初始化完成");
        return scheduler;
    }
}
