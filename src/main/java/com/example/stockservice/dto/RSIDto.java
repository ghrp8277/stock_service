package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RSIDto {
    private List<Double> rsi;
}
