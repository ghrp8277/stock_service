package com.example.stockservice.repository;

import com.example.stockservice.entity.Stock;
import com.example.stockservice.entity.StockData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    boolean existsByDateAndStock(String date, Stock stock);
}