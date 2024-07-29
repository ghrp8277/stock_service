package com.example.stockservice.exception.kafka;

import com.example.stockservice.exception.error.KafkaErrorCodes;
import com.example.stockservice.exception.error.KafkaErrorMessages;
import com.example.stockservice.exception.error.KafkaException;

public class MACDException extends KafkaException {
    public MACDException() {
        super(KafkaErrorMessages.MACD_DESERIALIZATION_MESSAGE, KafkaErrorCodes.MACD_DESERIALIZATION_CODE);
    }
}
