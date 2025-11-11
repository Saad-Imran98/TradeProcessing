package com.trade.tradeprocessing.services;

import com.trade.tradeprocessing.events.TradeReceivedEvent;
import com.trade.tradeprocessing.models.Trade;
import com.trade.tradeprocessing.repositories.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TradeProcessingService {

    TradeRepository tradeRepository;

    TradeProcessingService(@Autowired TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Async
    @EventListener
    public void processTrade(TradeReceivedEvent event) {
        Trade tradeToProcess = event.getTrade();

        tradeToProcess.setStatus("DONE");

        tradeToProcess.setTimeProcessed(Instant.now());

        System.out.println("Service processed Trade ID: " + tradeToProcess.getId() +
                " in thread: " + Thread.currentThread().getName());

         tradeRepository.save(tradeToProcess);
    }
}
