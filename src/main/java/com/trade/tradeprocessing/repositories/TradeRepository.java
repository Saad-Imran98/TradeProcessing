package com.trade.tradeprocessing.repositories;

import com.trade.tradeprocessing.models.Trade;
import org.springframework.data.repository.CrudRepository;

public interface TradeRepository extends CrudRepository<Trade, Long> {

}
