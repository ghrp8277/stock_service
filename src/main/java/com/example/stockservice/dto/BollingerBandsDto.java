package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BollingerBandsDto {
    private List<Double> upperBand;
    private List<Double> middleBand;
    private List<Double> lowerBand;
}
