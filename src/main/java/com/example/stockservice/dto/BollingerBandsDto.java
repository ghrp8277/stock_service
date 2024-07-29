package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class BollingerBandsDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Double> upperBand;
    private List<Double> middleBand;
    private List<Double> lowerBand;
}
