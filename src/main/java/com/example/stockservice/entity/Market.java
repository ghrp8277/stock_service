package com.example.stockservice.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Data
@Table(name = "markets")
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "market")
    private Set<Stock> stocks;
}
