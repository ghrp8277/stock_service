package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MovingAverageDto {
    private List<Double> sma12;
    private List<Double> sma20;
    private List<Double> sma26;
}
