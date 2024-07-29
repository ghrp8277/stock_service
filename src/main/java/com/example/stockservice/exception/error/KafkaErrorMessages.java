package com.example.stockservice.exception.error;

public class KafkaErrorMessages {
    public static final String REDIS_CONNECTION_MESSAGE = "Redis connection error";
    public static final String BOLLINGER_BANDS_DESERIALIZATION_MESSAGE = "Error deserializing Bollinger Bands data";
    public static final String SMA_DESERIALIZATION_MESSAGE = "Error deserializing SMA data";
    public static final String MACD_DESERIALIZATION_MESSAGE = "Error deserializing MACD data";
    public static final String RSI_DESERIALIZATION_MESSAGE = "Error deserializing RSI data";
}
