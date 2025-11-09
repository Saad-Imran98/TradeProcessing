package com.trade.tradeprocessing.controllers;

import com.trade.tradeprocessing.models.Status;
import com.trade.tradeprocessing.models.Trade;
import com.trade.tradeprocessing.services.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TradeController {

    @Autowired
    TradeService tradeService;

    @GetMapping("/get")
    List<Trade> getTrades(){
        return tradeService.getTrades();
    }

    @PostMapping("/postTrade")
    ResponseEntity<Void> postTrade(@RequestBody Trade trade){
        trade.setStatus(Status.Queued);
        // post trade
        tradeService.postTrade(trade);
        // send response
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
