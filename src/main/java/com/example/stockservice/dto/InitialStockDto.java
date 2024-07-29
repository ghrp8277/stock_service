package com.example.stockservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitialStockDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<StockDto> stockData;
    private MovingAverageDto movingAverages;
    private BollingerBandsDto bollingerBands;
    private MACDDto macd;
    private RSIDto rsi;
}
