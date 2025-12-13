package pl.pw.edu.po.search_engine.simplesearchengine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 * Enables @Async annotation for background tasks like web crawling.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure thread pool for async tasks.
     * - Core pool: 2 threads always ready
     * - Max pool: 5 threads maximum
     * - Queue: 100 tasks can wait
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // Minimum threads
        executor.setMaxPoolSize(5);         // Maximum threads
        executor.setQueueCapacity(100);     // Queue size
        executor.setThreadNamePrefix("crawler-");
        executor.initialize();
        return executor;
    }
}

