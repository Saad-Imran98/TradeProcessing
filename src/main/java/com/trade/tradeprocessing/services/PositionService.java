package com.trade.tradeprocessing.services;

import com.trade.tradeprocessing.models.Position;
import com.trade.tradeprocessing.models.Trade;
import com.trade.tradeprocessing.repositories.PositionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PositionService {

    Logger log;
    TradeService tradeService;
    PositionRepository positionRepository;

    public PositionService(TradeService tradeService, PositionRepository positionRepository) {
        this.tradeService = tradeService;
        this.positionRepository = positionRepository;
        log = LoggerFactory.getLogger(PositionService.class);
    }

    @Scheduled(fixedRate = 30000)
    public void calculateAndPersistPositions() {
        log.info("START: Calculating scheduled position aggregation for persistence.");
        // 1. Retrieve all processed trades
        List<Trade> processedTrades = tradeService.getTrades();

        Map<String, BigDecimal> currentPositions = calculateNetQuantities(processedTrades); // Assume this method holds the aggregation logic

        // 3. Convert Map results to Position entities and Persist (UPSERT)
        for (Map.Entry<String, BigDecimal> entry : currentPositions.entrySet()) {
            String instrument = entry.getKey();
            BigDecimal netQuantity = entry.getValue();

            // Check if a Position record already exists (Optional optimization)
            Position position = positionRepository.findById(instrument)
                    .orElse(new Position()); // Create new if not found

            position.setInstrument(instrument);
            position.setNetQuantity(netQuantity);
            position.setLastUpdated(Instant.now());
            // calculate and set netNotionalUsd

            positionRepository.save(position); // Saves new or updates existing (UPSERT)
        }
        // log positions
        log.info("Current Positions: " + currentPositions);
        log.info("FINISH: Position Aggregation persisted to DB.");
    }

    private Map<String, BigDecimal> calculateNetQuantities(List<Trade> trades) {
        Map<String, BigDecimal> currentPositions = new ConcurrentHashMap<>();
        // Calculate net quantity (BUY minus SELL)
        for (Trade trade : trades) {
            String instrument = trade.getInstrument(); // Assuming 'instrument' field exists on Trade
            BigDecimal quantity = trade.getQuantity();

            // Determine the sign for aggregation
            BigDecimal netQuantity = trade.getSide().equalsIgnoreCase("BUY") ? quantity : quantity.negate();

            // Atomically update the map
            currentPositions.merge(instrument, netQuantity, BigDecimal::add);
        }

        return currentPositions;
    }

    /**
     * Updated method to query from the DB instead of an in-memory map (or keep both).
     */
    public List<Position> getAllPositionsFromDB() {
        return positionRepository.findAll();
    }


}
