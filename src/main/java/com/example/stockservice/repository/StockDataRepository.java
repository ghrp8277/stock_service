package com.example.stockservice.repository;

import com.example.common.Stock;
import com.example.common.StockData;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface StockDataRepository extends JpaRepository<StockData, Long> {
    List<StockData> findByStockAndStock_MarketNameAndDateAfter(Stock stock, String marketName, String date);
    List<StockData> findByStockAndDateAfter(Stock stock, String date);
    @Query("SELECT sd FROM StockData sd JOIN FETCH sd.movingAverage12 JOIN FETCH sd.movingAverage20 WHERE sd.stock = :stock AND sd.date > :startDate")
    List<StockData> findByStockAndDateAfterWithMovingAverages(@Param("stock") Stock stock, @Param("startDate") String startDate);

    @Query("SELECT sd.date AS date, sd.openPrice AS openPrice, sd.highPrice AS highPrice, sd.lowPrice AS lowPrice, sd.closePrice AS closePrice, sd.volume AS volume FROM StockData sd WHERE sd.stock.market.name = :marketName AND sd.stock.code = :code ORDER BY sd.date DESC")
    Page<Tuple> findPast30DaysByMarketAndCode(@Param("marketName") String marketName, @Param("code") String code, Pageable pageable);
}