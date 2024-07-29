package com.example.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class MovingAverageDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Double> sma12;
    private List<Double> sma20;
    private List<Double> sma26;
}
