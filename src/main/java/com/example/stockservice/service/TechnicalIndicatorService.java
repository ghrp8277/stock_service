package com.example.stockservice.service;

import com.example.stockservice.entity.StockData;
import com.example.stockservice.entity.TechnicalIndicators.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicalIndicatorService {

    public void calculateIndicators(StockData stockData, List<Double> prices) {
        MovingAverage ma = new MovingAverage();
        ma.calculate(prices);
        stockData.setMovingAverage20(ma.getResults().get("SMA20"));

        BollingerBands bb = new BollingerBands();
        bb.calculate(prices);
        stockData.setBollingerBands(bb);

        MACD macd = new MACD();
        macd.calculate(prices);
        stockData.setMacd(macd);

        RSI rsi = new RSI();
        rsi.calculate(prices);
        List<Double> rsiValues = rsi.getResults().get("RSI");
        if (rsiValues != null && !rsiValues.isEmpty()) {
            stockData.setRsi(rsiValues.getLast());
        } else {
            stockData.setRsi(null);
        }
    }
}
