package com.example.stockservice.grpc;

import com.example.grpc.*;
import com.example.stockservice.dto.StockDto;
import com.example.stockservice.service.StockService;
import com.example.stockservice.util.GrpcResponseHelper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
public class StockServiceTmpl extends StockServiceGrpc.StockServiceImplBase {
    @Autowired
    private StockService stockService;

    @Autowired
    private GrpcResponseHelper grpcResponseHelper;

    @Override
    public void getStockData(GetStockDataRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<StockDto> stockData = stockService.getStockData(
                    request.getSymbol(),
                    request.getTimeframe(),
                    request.getCount()
            );

            List<Map<String, Object>> responseData = stockData.stream().map(stock -> {
                Map<String, Object> map = new HashMap<>();
                map.put("date", stock.getDate());
                map.put("open_price", stock.getOpenPrice());
                map.put("high_price", stock.getHighPrice());
                map.put("low_price", stock.getLowPrice());
                map.put("close_price", stock.getClosePrice());
                map.put("volume", stock.getVolume());
                return map;
            }).collect(Collectors.toList());

            grpcResponseHelper.sendJsonResponse("stock_data", responseData, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }
}
