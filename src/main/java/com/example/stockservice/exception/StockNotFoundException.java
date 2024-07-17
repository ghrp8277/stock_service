package com.example.stockservice.exception;

import com.example.stockservice.exception.error.ErrorCodes;
import com.example.stockservice.exception.error.ErrorMessages;
import com.example.stockservice.exception.error.GrpcException;

public class StockNotFoundException extends GrpcException {
    public StockNotFoundException() {
        super(ErrorCodes.STOCK_NOT_FOUND_CODE, ErrorMessages.STOCK_NOT_FOUND);
    }
}
