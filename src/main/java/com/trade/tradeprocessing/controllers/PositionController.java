package com.trade.tradeprocessing.controllers;

import com.trade.tradeprocessing.models.Position;
import com.trade.tradeprocessing.services.PositionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PositionController {

    private PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/positions")
    public List<Position> getPositions() {
        return positionService.getAllPositionsFromDB();
    }
}
