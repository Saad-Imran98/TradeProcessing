package com.trade.tradeprocessing.services;

import com.trade.tradeprocessing.repositories.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.trade.tradeprocessing.models.Trade;

import java.util.List;

@Service
public class TradeService {

    @Autowired
    TradeRepository tradeRepository;

    public List<Trade> getTrades(){
        return (List<Trade>) tradeRepository.findAll();
    }

    public void postTrade(Trade trade) {
        tradeRepository.save(trade);
    }
}
