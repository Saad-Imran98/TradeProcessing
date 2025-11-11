package com.trade.tradeprocessing.events;

import com.trade.tradeprocessing.models.Trade;
import org.springframework.context.ApplicationEvent;

public class TradeReceivedEvent extends ApplicationEvent {
    private final Trade trade;

    public TradeReceivedEvent(Object source, Trade trade) {
        super(source);
        this.trade = trade;
    }

    public Trade getTrade() {
        return trade;
    }
}
