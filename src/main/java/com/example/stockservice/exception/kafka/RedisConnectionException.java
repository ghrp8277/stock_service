package com.example.stockservice.exception.kafka;

import com.example.stockservice.exception.error.KafkaErrorCodes;
import com.example.stockservice.exception.error.KafkaErrorMessages;
import com.example.stockservice.exception.error.KafkaException;

public class RedisConnectionException extends KafkaException {
    public RedisConnectionException(Throwable cause) {
        super(KafkaErrorMessages.REDIS_CONNECTION_MESSAGE + ": " + cause.getMessage(), KafkaErrorCodes.REDIS_CONNECTION_CODE);
    }
}
