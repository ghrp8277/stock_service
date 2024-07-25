package com.example.stockservice.repository;

import com.example.common.Market;
import com.example.common.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByCode(String code);
    List<Stock> findByMarket(Market market);
    Optional<Stock> findByCodeAndMarketName(String code, String marketName);
    Page<Stock> findByMarket(Market market, Pageable pageable);
    Page<Stock> findByNameContaining(String name, Pageable pageable);
    Page<Stock> findAllByCodeContaining(String code, Pageable pageable);
}