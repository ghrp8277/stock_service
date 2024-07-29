package com.example.stockservice.service;

import com.example.stockservice.constants.KafkaConstants;
import com.example.stockservice.dto.InitialStockDto;
import com.example.stockservice.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
}
