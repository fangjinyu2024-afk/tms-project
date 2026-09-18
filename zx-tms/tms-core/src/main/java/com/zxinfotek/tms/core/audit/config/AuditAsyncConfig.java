package com.zxinfotek.tms.core.audit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 日志异步写入线程池：业务操作日志队列满时由调用线程兜底写入，接口访问日志队列满时直接丢弃并告警。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Configuration
public class AuditAsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AuditAsyncConfig.class);

    @Bean(name = "operLogExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor operLogExecutor() {
        return new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), namedFactory("oper-log"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(name = "apiAccessLogExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor apiAccessLogExecutor() {
        return new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5000), namedFactory("api-log"),
                (runnable, executor) -> log.warn("接口访问日志队列已满，本条记录被丢弃"));
    }

    private static java.util.concurrent.ThreadFactory namedFactory(String prefix) {
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-writer");
            thread.setDaemon(true);
            return thread;
        };
    }
}
