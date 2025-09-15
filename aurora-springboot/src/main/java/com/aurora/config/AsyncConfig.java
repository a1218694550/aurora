package com.aurora.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
@Slf4j
public class AsyncConfig {

    @Value("${async.corePoolSize:10}")
    private Integer corePoolSize = 10;
    @Value("${async.maxPoolSize:20}")
    private Integer maxPoolSize = 20;
    @Value("${async.queueCapacity:100}")
    private Integer queueCapacity = 100;
    @Value("${async.keepAliveSeconds:300}")
    private Integer keepAliveSeconds = 300;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("async-task-thread-");
        // 设置拒绝策略，防止系统过载
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("初始化任务线程池,核心线程数["+corePoolSize+"] 最大线程数["+maxPoolSize+"] 队列大小["+queueCapacity+"] 存活时间["+keepAliveSeconds+"]");
        return executor;
    }
}