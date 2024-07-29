package com.example.stockservice.exception.kafka;

import com.example.stockservice.exception.error.KafkaErrorCodes;
import com.example.stockservice.exception.error.KafkaErrorMessages;
import com.example.stockservice.exception.error.KafkaException;

public class SMAException extends KafkaException {
    public SMAException() {
        super(KafkaErrorMessages.SMA_DESERIALIZATION_MESSAGE, KafkaErrorCodes.SMA_DESERIALIZATION_CODE);
    }
}
