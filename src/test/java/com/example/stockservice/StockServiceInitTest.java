package com.example.stockservice;

import com.example.stockservice.dto.InitialStockDto;
import com.example.stockservice.repository.StockDataRepository;
import com.example.stockservice.repository.StockRepository;
import com.example.stockservice.service.KafkaProducerService;
import com.example.stockservice.service.StockService;
import com.example.stockservice.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StockServiceInitTest {
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private JsonUtil jsonUtil;

    @Test
    public void testProcessAndSendDailyData() {
        InitialStockDto result = stockService.getInitialStockData("test", "KOSPI", "005930", "1month");
//        InitialStockDto result = stockService.getInitialStockData("KOSPI", "005930", "1year");
//        InitialStockDto result = stockService.getInitialStockData("KOSPI", "005930", "3years");

        String jsonMessage = jsonUtil.toJson(result);
        kafkaProducerService.sendInitialStockDataMessage(jsonMessage);

        // Validate the result
        assertNotNull(result);
        assertNotNull(result.getStockData());
        assertNotNull(result.getMovingAverages());
        assertNotNull(result.getBollingerBands());
        assertNotNull(result.getMacd());
        assertNotNull(result.getRsi());
    }
}
