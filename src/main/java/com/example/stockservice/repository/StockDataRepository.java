package com.example.stockservice.repository;

import com.example.stockservice.entity.Stock;
import com.example.stockservice.entity.StockData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findByStockAndStock_MarketNameAndDateAfter(Stock stock, String marketName, String date);
    List<StockData> findByStockAndDateAfter(Stock stock, String date);
    @Query("SELECT sd FROM StockData sd JOIN FETCH sd.movingAverage12 JOIN FETCH sd.movingAverage20 WHERE sd.stock = :stock AND sd.date > :startDate")
    List<StockData> findByStockAndDateAfterWithMovingAverages(@Param("stock") Stock stock, @Param("startDate") String startDate);
}