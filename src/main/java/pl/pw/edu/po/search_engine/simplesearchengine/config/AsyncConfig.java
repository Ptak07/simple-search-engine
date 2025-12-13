package pl.pw.edu.po.search_engine.simplesearchengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 * Enables the use of @Async annotation for background tasks such as web crawling.
 *
 * <p>Thread pool configuration:
 * <ul>
 *   <li>Core pool size: 2 threads always active</li>
 *   <li>Max pool size: 5 threads maximum</li>
 *   <li>Queue capacity: 100 tasks can wait in queue</li>
 * </ul>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_NAME_PREFIX = "crawler-";

    /**
     * Configures the thread pool task executor for asynchronous operations.
     *
     * <p>Thread pool behavior:
     * <ul>
     *   <li>When tasks <= CORE_POOL_SIZE: uses core threads</li>
     *   <li>When tasks > CORE_POOL_SIZE: queues tasks (up to QUEUE_CAPACITY)</li>
     *   <li>When queue is full: creates new threads (up to MAX_POOL_SIZE)</li>
     *   <li>When all threads busy and queue full: rejects new tasks</li>
     * </ul>
     *
     * @return configured executor for async tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}

