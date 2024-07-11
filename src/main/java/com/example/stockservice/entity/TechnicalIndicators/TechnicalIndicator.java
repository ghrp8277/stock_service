package com.example.stockservice.entity.TechnicalIndicators;

import java.util.List;
import java.util.Map;

public interface TechnicalIndicator {
    void calculate(List<Double> prices);
    Map<String, List<Double>> getResults();
}
