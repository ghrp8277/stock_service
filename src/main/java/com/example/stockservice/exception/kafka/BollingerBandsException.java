package com.example.stockservice.exception.kafka;

import com.example.stockservice.exception.error.KafkaErrorCodes;
import com.example.stockservice.exception.error.KafkaErrorMessages;
import com.example.stockservice.exception.error.KafkaException;

public class BollingerBandsException extends KafkaException {
    public BollingerBandsException() {
        super(KafkaErrorMessages.BOLLINGER_BANDS_DESERIALIZATION_MESSAGE, KafkaErrorCodes.BOLLINGER_BANDS_DESERIALIZATION_CODE);
    }
}
