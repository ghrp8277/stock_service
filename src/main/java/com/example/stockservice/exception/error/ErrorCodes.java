package com.example.stockservice.exception.error;

import io.grpc.Status;

public class ErrorCodes {
    public static final Status.Code STOCK_NOT_FOUND_CODE = Status.Code.NOT_FOUND;
    public static final Status.Code MARKET_NOT_FOUND_CODE = Status.Code.NOT_FOUND;
}