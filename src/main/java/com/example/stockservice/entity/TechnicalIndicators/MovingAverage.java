package com.example.stockservice.entity.TechnicalIndicators;

import lombok.Data;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Embeddable
@Data
public class MovingAverage implements TechnicalIndicator {
    @ElementCollection
    @CollectionTable(name = "sma_12", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private List<Double> sma12;

    @ElementCollection
    @CollectionTable(name = "sma_20", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private List<Double> sma20;

    @ElementCollection
    @CollectionTable(name = "sma_26", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private List<Double> sma26;

    @Override
    public void calculate(List<Double> prices) {
        sma12 = calculateSMA(prices, 12);
        sma20 = calculateSMA(prices, 20);
        sma26 = calculateSMA(prices, 26);
    }

    private List<Double> calculateSMA(List<Double> prices, int period) {
        List<Double> sma = new ArrayList<>();
        for (int i = 0; i <= prices.size() - period; i++) {
            double sum = 0;
            for (int j = i; j < i + period; j++) {
                sum += prices.get(j);
            }
            sma.add(sum / period);
        }
        return sma;
    }

    @Override
    public Map<String, List<Double>> getResults() {
        Map<String, List<Double>> results = new HashMap<>();
        results.put("SMA12", sma12);
        results.put("SMA20", sma20);
        results.put("SMA26", sma26);
        return results;
    }
}
