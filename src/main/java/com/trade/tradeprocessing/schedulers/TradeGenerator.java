package com.trade.tradeprocessing.schedulers;

import com.trade.tradeprocessing.events.TradeReceivedEvent;
import com.trade.tradeprocessing.models.Status;
import com.trade.tradeprocessing.models.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TradeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TradeGenerator.class);
    private final String[] instruments = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "FB", "NFLX", "NVDA", "BABA", "INTC"};
    private final String[] counterParties = {"JP Morgan", "Goldman Sachs", "Morgan Stanley", "Citibank", "Bank of America"};
    private final String[] currencies = {"USD", "EUR", "GBP", "JPY", "AUD"};
    private final ApplicationEventPublisher eventPublisher;
    private BlockingQueue<Trade> tradeQueue;

    public TradeGenerator(@Autowired ApplicationEventPublisher eventPublisher, @Autowired BlockingQueue<Trade> tradeQueue) {
        this.eventPublisher = eventPublisher;
        this.tradeQueue = tradeQueue;
    }

    public Trade createRandomTrade(){
        return Trade.builder()
                .status(Status.Queued)
                .tradeDate(LocalDate.now())
                .counterparty(counterParties[new Random().nextInt(counterParties.length)])
                .createdAt(new Date().toInstant())
                .currency(currencies[new Random().nextInt(currencies.length)])
                .side(new Random().nextBoolean() ? "BUY" : "SELL")
                .quantity(new java.math.BigDecimal(new Random().nextInt(10000)))
                .price(new java.math.BigDecimal(new Random().nextInt(500)))
                .instrument(instruments[new Random().nextInt(instruments.length)])
                .build();
    }

    @Scheduled(initialDelay = 100, fixedRate = 5000) // every 5 seconds
    public void pushRandomTrade() throws InterruptedException {
        Trade randomTrade = createRandomTrade();
        try {
            tradeQueue.put(randomTrade);
            logger.info("PRODUCER: Trade ID " + randomTrade.getId() + " pushed to queue.");
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue trade: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
