package com.trade.tradeprocessing.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private LocalDate tradeDate;
    private String instrument;
    private String side; // "BUY"/"SELL"
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private String counterparty;
    private String status;
    private Instant createdAt;
}
