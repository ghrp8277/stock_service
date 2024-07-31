package com.example.stockservice;

import com.example.common.Stock;
import com.example.common.StockData;
import com.example.stockservice.dto.DailyStockDataDto;
import com.example.stockservice.dto.StockDto;
import com.example.stockservice.repository.StockDataRepository;
import com.example.stockservice.repository.StockRepository;
import com.example.stockservice.service.KafkaProducerService;
import com.example.stockservice.service.StockService;
import com.example.stockservice.util.JsonUtil;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StockServiceIntegrationTest {

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

    public StockDto convertToDto(Tuple tuple) {
        return new StockDto(
            tuple.get("date", String.class),
            tuple.get("openPrice", Integer.class),
            tuple.get("highPrice", Integer.class),
            tuple.get("lowPrice", Integer.class),
            tuple.get("closePrice", Integer.class),
            tuple.get("volume", Integer.class)
        );
    }

    public List<StockDto> convertToDtoList(List<Tuple> tuples) {
        return tuples.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Test
    public void testConvertToDtoList() {
        Stock stock = stockRepository.findByCode("005930").orElseThrow(() -> new RuntimeException("Stock not found"));
        String marketName = stock.getMarket().getName();
        Pageable pageable = PageRequest.of(0, 30);

        List<Tuple> stockDataTuples = stockDataRepository.findPast30DaysByMarketAndCode(marketName, "005930", pageable).getContent();
        List<StockDto> stockDtoList = this.convertToDtoList(stockDataTuples);

        assertEquals(stockDataTuples.size(), stockDtoList.size());
        for (int i = 0; i < stockDataTuples.size(); i++) {
            Tuple tuple = stockDataTuples.get(i);
            StockDto stockDto = stockDtoList.get(i);
            assertEquals(tuple.get("date", String.class), stockDto.getDate());
            assertEquals(tuple.get("openPrice", Integer.class), stockDto.getOpenPrice());
            assertEquals(tuple.get("highPrice", Integer.class), stockDto.getHighPrice());
            assertEquals(tuple.get("lowPrice", Integer.class), stockDto.getLowPrice());
            assertEquals(tuple.get("closePrice", Integer.class), stockDto.getClosePrice());
            assertEquals(tuple.get("volume", Integer.class), stockDto.getVolume());
        }
    }

    @Test
    public void testProcessAndSendDailyData() {
        DailyStockDataDto result = stockService.getDailyStockData("005930");
        String jsonMessage = jsonUtil.toJson(result);
        kafkaProducerService.sendDailyStockDataMessage(jsonMessage);

        // Validate the result
        assertNotNull(result);
        assertNotNull(result.getStockData());
        assertNotNull(result.getMovingAverages());
        assertNotNull(result.getBollingerBands());
        assertNotNull(result.getMacd());
        assertNotNull(result.getRsi());
    }
}
