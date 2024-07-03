package com.example.stockservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "current_price", nullable = false)
    private double currentPrice;

    @Column(name = "market_cap", nullable = false)
    private long marketCap;

    @Column(name = "volume", nullable = false)
    private long volume;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "establishment_date", nullable = false)
    private String establishmentDate;

    @Column(name = "headquarters", nullable = false)
    private String headquarters;

    @Column(name = "ceo", nullable = false)
    private String ceo;

    @Column(name = "website", nullable = false)
    private String website;

    @ManyToOne
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;
}
