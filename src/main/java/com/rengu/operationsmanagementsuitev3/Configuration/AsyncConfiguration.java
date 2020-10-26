package com.rengu.operationsmanagementsuitev3.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-11 16:43
 **/

@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    /**
     * The {@link Executor} instance to be used when processing async
     * method invocations.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(50);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadNamePrefix("OMS-Thread");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;

    }
}
