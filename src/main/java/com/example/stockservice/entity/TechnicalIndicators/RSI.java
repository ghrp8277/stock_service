package com.example.stockservice.entity.TechnicalIndicators;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Embeddable
@Data
public class RSI implements TechnicalIndicator {
    @ElementCollection
    @CollectionTable(name = "rsi_values", joinColumns = @JoinColumn(name = "stock_data_id"))
    @Column(name = "value")
    private List<Double> rsi = new ArrayList<>();

    @Override
    public void calculate(List<Double> prices) {
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(-change);
            }
        }
        double avgGain = gains.stream().mapToDouble(val -> val).average().orElse(0.0);
        double avgLoss = losses.stream().mapToDouble(val -> val).average().orElse(0.0);

        for (int i = 0; i < gains.size(); i++) {
            double rs = avgGain / avgLoss;
            double rsiValue = 100 - (100 / (1 + rs));
            rsi.add(rsiValue);
            avgGain = (avgGain * 13 + gains.get(i)) / 14;
            avgLoss = (avgLoss * 13 + losses.get(i)) / 14;
        }
    }

    @Override
    public Map<String, List<Double>> getResults() {
        Map<String, List<Double>> results = new HashMap<>();
        results.put("RSI", rsi);
        return results;
    }
}
