package com.example.stockservice.repository;

import com.example.stockservice.entity.Market;
import com.example.stockservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByCode(String code);
    List<Stock> findByNameContaining(String name);
    List<Stock> findAllByCode(String code);
    List<Stock> findByMarketName(String market);
    List<Stock> findByMarket(Market market);
    Optional<Stock> findByCodeAndMarketName(String code, String marketName);
}