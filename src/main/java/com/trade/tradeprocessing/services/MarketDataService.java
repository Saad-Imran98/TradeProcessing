package com.trade.tradeprocessing.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class MarketDataService {

    private final Random random = new Random();

    /**
     * Simulates fetching a random FX rate for a given currency pair (e.g., EUR/USD).
     * Since the trade only has quantity and price, we'll assume the price is in a
     * foreign currency (FX) and we need the FXRate to convert it to USD.
     * For simplicity, this method returns a rate between 1.05 and 1.15.
     */
    @Async
    public CompletableFuture<BigDecimal> getFxRateAsync(String currencyPair) {
        // Generate a rate between 1.05 and 1.15
        double rate = 1.05 + (1.15 - 1.05) * random.nextDouble();

        // Return the rate rounded to 4 decimal places
        return CompletableFuture
                .completedFuture(BigDecimal.valueOf(rate).setScale(4, BigDecimal.ROUND_HALF_UP));
    }

    public BigDecimal getFxRate(String currencyPair) {
        // Generate a rate between 1.05 and 1.15
        double rate = 1.05 + (1.15 - 1.05) * random.nextDouble();

        // Return the rate rounded to 4 decimal places
        return BigDecimal.valueOf(rate).setScale(4, BigDecimal.ROUND_HALF_UP);
    }
}
