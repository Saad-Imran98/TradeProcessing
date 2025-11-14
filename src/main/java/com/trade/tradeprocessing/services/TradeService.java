package com.trade.tradeprocessing.services;

import com.trade.tradeprocessing.repositories.TradeRepository;
import org.springframework.stereotype.Service;
import com.trade.tradeprocessing.models.Trade;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TradeService {

    private TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<Trade> getTrades(){
        return (List<Trade>) tradeRepository.findAll();
    }

    public void postTrade(Trade trade) {
        tradeRepository.save(trade);
    }
}
