package com.trade.tradeprocessing.repositories;

import com.trade.tradeprocessing.models.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, String> { }
