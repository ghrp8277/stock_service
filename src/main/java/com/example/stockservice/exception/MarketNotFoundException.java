package com.example.stockservice.exception;

import com.example.stockservice.exception.error.ErrorCodes;
import com.example.stockservice.exception.error.ErrorMessages;
import com.example.stockservice.exception.error.GrpcException;

public class MarketNotFoundException extends GrpcException {
    public MarketNotFoundException() {
        super(ErrorCodes.MARKET_NOT_FOUND_CODE, ErrorMessages.MARKET_NOT_FOUND);
    }
}
