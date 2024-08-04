package com.example.stockservice.service;

import com.example.stockservice.constants.KafkaConstants;
import com.example.stockservice.dto.DailyStockDataDto;
import com.example.stockservice.dto.InitialStockDto;
import com.example.stockservice.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.example.common.Stock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KafkaConsumerService {
    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private StockService stockService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @KafkaListener(topics = KafkaConstants.INITIAL_DATA_REQUEST_TOPIC, groupId = KafkaConstants.INITIAL_DATA_GROUP_ID)
    public void consumeInitialDataRequest(String message) {
        String timeframe = jsonUtil.getValueByKey(message, "timeframe");
        String marketName = jsonUtil.getValueByKey(message, "marketName");
        String code = jsonUtil.getValueByKey(message, "code");
        InitialStockDto initialStockDto = stockService.getInitialStockData(marketName, code, timeframe);
        String jsonMessage = jsonUtil.toJson(initialStockDto);
        kafkaProducerService.sendInitialStockDataMessage(jsonMessage);
    }

    @KafkaListener(topics = KafkaConstants.DAILY_DATA_REQUEST_TOPIC, groupId = KafkaConstants.DAILY_DATA_GROUP_ID)
    public void consumeDailyDataRequest(String message) {
        Map<String, Object> messageMap = jsonUtil.parseJsonToMap(message);
        String symbol = (String) messageMap.get("symbol");
        DailyStockDataDto dailyStockDataDto = stockService.getDailyStockData(symbol);
        String jsonMessage = jsonUtil.toJson(dailyStockDataDto);
        kafkaProducerService.sendDailyStockDataMessage(jsonMessage);
    }

    @KafkaListener(topics = KafkaConstants.SOCIAL_STOCK_REQUEST_TOPIC, groupId = KafkaConstants.SOCIAL_STOCK_GROUP_ID)
    public void consumeSocialStockRequest(String message) {
        String market = jsonUtil.getValueByKey(message, "market");
        List<Stock> stocks = stockService.getStocksByMarket(market);
        List<Map<String, Object>> responseData = stocks.stream()
        .map(stock -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", stock.getCode());
            map.put("name", stock.getName());
            return map;
        })
        .toList();
        String jsonMessage = jsonUtil.toJson(responseData);
        kafkaProducerService.sendStockMessage(jsonMessage);
    }
}
