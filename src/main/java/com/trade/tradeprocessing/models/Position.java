package com.trade.tradeprocessing.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Position {
    @Id
    private String instrument;
    private BigDecimal netQuantity;
    private BigDecimal netNotionalUsd;
    private Instant lastUpdated;
    public Position() {}

    public Position(String instrument, BigDecimal netQuantity, BigDecimal netNotionalUsd) {
        this.instrument = instrument;
        this.netQuantity = netQuantity;
        this.netNotionalUsd = netNotionalUsd;
        this.lastUpdated = Instant.now();
    }

}
