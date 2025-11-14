package com.trade.tradeprocessing.services;

import com.trade.tradeprocessing.models.Status;
import com.trade.tradeprocessing.models.Trade;
import com.trade.tradeprocessing.repositories.TradeRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TradeProcessingService {

    private final TradeRepository tradeRepository;
    private final Logger log;
    private final BlockingQueue<Trade> tradeQueue;
    private final TaskExecutor tradeProcessingExecutor;
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTimeNs = new AtomicLong(0);
    private long startTime;
    private MarketDataService marketDataService;

    TradeProcessingService(TradeRepository tradeRepository,
                           BlockingQueue<Trade> tradeQueue,
                           TaskExecutor tradeProcessingExecutor,
                           MarketDataService marketDataService) {
        log = LoggerFactory.getLogger(TradeProcessingService.class);
        this.tradeRepository = tradeRepository;
        this.tradeQueue = tradeQueue;
        this.tradeProcessingExecutor = tradeProcessingExecutor;
        this.marketDataService = marketDataService;
    }

    // Starts the worker threads when the application starts
    @PostConstruct
    public void startConsumers() {
        // Start measuring throughput
        this.startTime = System.currentTimeMillis();

        // Submit N consumer tasks to the thread pool (N = CorePoolSize from config)
        int numWorkers = ((ThreadPoolTaskExecutor) tradeProcessingExecutor).getCorePoolSize();
        for (int i = 0; i < numWorkers; i++) {
            tradeProcessingExecutor.execute(this::consumeTrades);
        }
        log.info("{} worker threads started for trade processing.", numWorkers);

        // Optional: Schedule a thread to log throughput metrics periodically
        tradeProcessingExecutor.execute(this::logThroughput);
    }

    private void logThroughput() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Wait for 10 seconds
                Thread.sleep(30000);

                long duration = System.currentTimeMillis() - startTime;
                if (duration > 0) {
                    double durationSeconds = duration / 1000.0;
                    double throughput = processedCount.get() / durationSeconds;

                    log.info("--- METRICS ---");
                    log.info("Total Trades Processed: {}", processedCount.get());
                    log.info("Current Throughput: {:.2f} trades/second", throughput);
                    log.info("Queue Size: {}", tradeQueue.size());
                    log.info("--------------");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // The continuous CONSUMER loop
    private void consumeTrades() {
        log.info("CONSUMER thread {} started.", Thread.currentThread().getName());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // CONSUMER takes a trade from the queue. 'take' blocks until a trade is available.
                Trade trade = tradeQueue.take();

                long latencyStart = System.nanoTime();

                // Execute the processing pipeline
                processTradePipeline(trade);

                long latencyEnd = System.nanoTime();
                long latencyNs = latencyEnd - latencyStart;

                // Log and update metrics
                totalProcessingTimeNs.addAndGet(latencyNs); // <--- RECORD TIME
                log.debug("Processed Trade ID {} in {} nanoseconds.", trade.getId(), latencyNs);
                processedCount.incrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Consumer thread interrupted. Shutting down.");
            } catch (Exception e) {
                log.error("Unhandled exception during trade processing.", e);
            }
        }
    }

    public boolean isValid(Trade trade) {
        if (trade.getQuantity() == null || trade.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        String side = trade.getSide();
        return side.equals("BUY") || side.equals("SELL");
    }

    public void processTradePipeline(Trade pendingTrade) {
        log.debug("START Processing Trade ID: {} by thread {}", pendingTrade.getId(), Thread.currentThread().getName());
        pendingTrade.setStatus(Status.Processing);
        tradeRepository.save(pendingTrade);

        if (!isValid(pendingTrade)) {
            pendingTrade.setStatus(Status.Failed);
            pendingTrade.setTimeProcessed(Instant.now());
            tradeRepository.save(pendingTrade);
            log.warn("Trade ID {} REJECTED: Failed validation (Side: {}, Qty: {}).",
                    pendingTrade.getId(), pendingTrade.getSide(), pendingTrade.getQuantity());
            return;
        }

        // --- ASYNCHRONOUS ENRICHMENT STAGE ---
        try {
            // 3. Kick off the asynchronous FX rate fetch
            CompletableFuture<BigDecimal> fxFuture = marketDataService.getFxRateAsync("EURUSD");

            // Wait for the result from the async thread.
            // This is the point where the consumer thread waits for the background task to complete.
            BigDecimal fxRate = fxFuture.get();

            // Calculate and enrich
            enrichTrade(pendingTrade, fxRate);

        } catch (Exception e) {
            log.error("Trade ID {} FAILED during ASYNC enrichment: {}", pendingTrade.getId(), e.getMessage());
            // Optionally: set status to FAILED and persist
            pendingTrade.setStatus("FAILED");
            tradeRepository.save(pendingTrade);
            return;
        }

        pendingTrade.setStatus(Status.Done);
        pendingTrade.setTimeProcessed(Instant.now());
        tradeRepository.save(pendingTrade);
    }

    /**
     * Performs the required enrichment calculation: notionalUsd = quantity × price × fxRate
     */
    private void enrichTrade(Trade trade, BigDecimal fxRate) {
        // Placeholder currency pair since the Trade object doesn't have it yet.
        // In a real system, this would be based on the trade's currency.

        // notionalUsd = quantity × price × fxRate
        BigDecimal notional = trade.getQuantity()
                .multiply(trade.getPrice())
                .multiply(fxRate)
                .setScale(2, 0); // Round to 2 decimal places for USD

        trade.setNotionalUsd(notional);
    }

}
