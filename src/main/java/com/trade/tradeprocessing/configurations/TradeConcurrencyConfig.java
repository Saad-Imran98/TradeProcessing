package com.trade.tradeprocessing.configurations;

import com.trade.tradeprocessing.models.Trade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class TradeConcurrencyConfig {

    // 1. Thread-Safe Queue (The Buffer)
    @Bean
    public BlockingQueue<Trade> tradeQueue() {
        // LinkedBlockingQueue is a good choice for a FIFO queue
        // Capacity can be set here, e.g., 1000, to prevent unbounded memory growth.
        return new LinkedBlockingQueue<>(1000);
    }

    // 2. Thread Pool (The Workers)
    @Bean
    public TaskExecutor tradeProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Number of permanent worker threads
        executor.setMaxPoolSize(10); // Max number of threads it can scale to
        executor.setQueueCapacity(0); // We use our dedicated BlockingQueue, so set this to 0
        executor.setThreadNamePrefix("Trade-Processor-");
        executor.initialize();
        return executor;
    }
}
