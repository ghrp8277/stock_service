package com.example.stockservice.entity;

import com.example.stockservice.entity.TechnicalIndicators.BollingerBands;
import com.example.stockservice.entity.TechnicalIndicators.MACD;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(
        name = "stock_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "stock_id"}),
        indexes = {
           @Index(name = "idx_stock_data_date", columnList = "date"),
           @Index(name = "idx_stock_data_stock_id", columnList = "stock_id")
       }
)
public class StockData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "open_price", nullable = false)
    private Integer openPrice;

    @Column(name = "high_price", nullable = false)
    private Integer highPrice;

    @Column(name = "low_price", nullable = false)
    private Integer lowPrice;

    @Column(name = "close_price", nullable = false)
    private Integer closePrice;

    @Column(name = "volume", nullable = false)
    private Integer volume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "moving_average_12", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private Set<Double> movingAverage12;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "moving_average_20", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private Set<Double> movingAverage20;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "moving_average_26", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private Set<Double> movingAverage26;

    @Embedded
    private BollingerBands bollingerBands;

    @Embedded
    private MACD macd;

    @Column(name = "rsi")
    private Double rsi;
}