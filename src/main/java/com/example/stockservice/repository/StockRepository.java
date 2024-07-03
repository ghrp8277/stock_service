package com.example.stockservice.repository;

import com.example.stockservice.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByCode(String code);
}