package com.example.stockservice.service;

import com.example.stockservice.constants.KafkaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendInitialStockDataMessage(String message) {
        kafkaTemplate.send(KafkaConstants.INITIAL_DATA_TOPIC, message);
    }
}
