package com.trade.tradeprocessing.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private String tradeId;
    private LocalDate tradeDate;
    private String instrument;
    private String side; // "BUY"/"SELL"
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private String counterparty;
    private String status; // PENDING, PROCESSED, REJECTED
    private Instant createdAt;
    // getters/setters, equals/hashCode
}
