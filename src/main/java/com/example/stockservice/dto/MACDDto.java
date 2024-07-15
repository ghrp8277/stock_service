package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MACDDto {
    private List<Double> macdLine;
    private List<Double> signalLine;
    private List<Double> histogram;
}
