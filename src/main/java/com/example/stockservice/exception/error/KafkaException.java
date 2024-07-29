package com.example.stockservice.exception.error;

import lombok.Getter;

@Getter
public class KafkaException extends RuntimeException {
    private final int statusCode;

    public KafkaException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}
