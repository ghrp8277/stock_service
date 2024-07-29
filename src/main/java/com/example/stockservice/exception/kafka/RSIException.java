package com.example.stockservice.exception.kafka;

import com.example.stockservice.exception.error.KafkaErrorCodes;
import com.example.stockservice.exception.error.KafkaErrorMessages;
import com.example.stockservice.exception.error.KafkaException;

public class RSIException extends KafkaException {
    public RSIException() {
        super(KafkaErrorMessages.RSI_DESERIALIZATION_MESSAGE, KafkaErrorCodes.RSI_DESERIALIZATION_CODE);
    }
}
