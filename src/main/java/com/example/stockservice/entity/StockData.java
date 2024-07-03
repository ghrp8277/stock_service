package com.example.stockservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "stock_data", uniqueConstraints = @UniqueConstraint(columnNames = {"date", "stock_id"}))
public class StockData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "open_price", nullable = false)
    private int openPrice;

    @Column(name = "high_price", nullable = false)
    private int highPrice;

    @Column(name = "low_price", nullable = false)
    private int lowPrice;

    @Column(name = "close_price", nullable = false)
    private int closePrice;

    @Column(name = "volume", nullable = false)
    private int volume;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;
}