package com.example.stockservice.grpc;

import com.example.grpc.*;
import com.example.stockservice.dto.StockDto;
import com.example.stockservice.entity.Market;
import com.example.stockservice.entity.Stock;
import com.example.stockservice.service.StockService;
import com.example.stockservice.util.GrpcResponseHelper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

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
    public void getMarkets(Empty request, StreamObserver<Response> responseObserver) {
        try {
            List<Market> markets = stockService.getMarkets();

            List<Map<String, String>> responseData = markets.stream()
                    .map(market -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("name", market.getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("markets", responseData);

            grpcResponseHelper.sendJsonResponse("markets", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    @Override
    public void getStocksByMarket(GetStocksByMarketRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<Stock> stocks = stockService.getStocksByMarket(request.getMarketName());

            List<Map<String, Object>> responseData = stocks.stream()
                    .map(stock -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", stock.getCode());
                        map.put("name", stock.getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("market_name", request.getMarketName());
            response.put("stocks", responseData);

            grpcResponseHelper.sendJsonResponse("stocks", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    @Override
    public void getStockDataByMarketAndCode(GetStockDataByMarketAndCodeRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<StockDto> stockDataList = stockService.getStockDataByMarketAndCode(
                    request.getMarketName(),
                    request.getCode(),
                    request.getTimeframe()
            );

            List<Map<String, Object>> responseData = stockDataList.stream()
                    .map(stockData -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", stockData.getDate());
                        map.put("open_price", stockData.getOpenPrice());
                        map.put("high_price", stockData.getHighPrice());
                        map.put("low_price", stockData.getLowPrice());
                        map.put("close_price", stockData.getClosePrice());
                        map.put("volume", stockData.getVolume());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("market_name", request.getMarketName());
            response.put("stock_code", request.getCode());
            response.put("stocks", responseData);

            grpcResponseHelper.sendJsonResponse("stocks", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    @Override
    public void getAllStocksByMarket(GetAllStocksByMarketRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<String> sortParams = request.getSortList();
            String[] sortArray = sortParams.toArray(new String[0]);

            Page<Stock> stocksPage = stockService.getAllStocksByMarket(
                    request.getMarketName(),
                    request.getPage(),
                    request.getSize(),
                    sortArray
            );

            List<Map<String, Object>> responseData = stocksPage.getContent().stream()
                    .map(stock -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", stock.getCode());
                        map.put("name", stock.getName());
                        map.put("market_name", stock.getMarket().getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("market_name", request.getMarketName());
            response.put("stocks", responseData);
            response.put("total_pages", stocksPage.getTotalPages());
            response.put("total_elements", stocksPage.getTotalElements());

            grpcResponseHelper.sendJsonResponse("all_stocks_by_market", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    @Override
    public void searchStocksByName(SearchStocksByNameRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<String> sortParams = request.getSortList();
            String[] sortArray = sortParams.toArray(new String[0]);

            Page<Stock> stocksPage = stockService.searchStocksByName(
                    request.getName(),
                    request.getPage(),
                    request.getSize(),
                    sortArray
            );

            List<Map<String, Object>> responseData = stocksPage.getContent().stream()
                    .map(stock -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", stock.getCode());
                        map.put("name", stock.getName());
                        map.put("market_name", stock.getMarket().getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("stocks", responseData);
            response.put("total_pages", stocksPage.getTotalPages());
            response.put("total_elements", stocksPage.getTotalElements());

            grpcResponseHelper.sendJsonResponse("stocks_by_name", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }

    @Override
    public void searchStocksByCode(SearchStocksByCodeRequest request, StreamObserver<Response> responseObserver) {
        try {
            List<String> sortParams = request.getSortList();
            String[] sortArray = sortParams.toArray(new String[0]);

            Page<Stock> stocksPage = stockService.searchStocksByCode(
                    request.getCode(),
                    request.getPage(),
                    request.getSize(),
                    sortArray
            );

            List<Map<String, Object>> responseData = stocksPage.getContent().stream()
                    .map(stock -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("code", stock.getCode());
                        map.put("name", stock.getName());
                        map.put("market_name", stock.getMarket().getName());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("code", request.getCode());
            response.put("stocks", responseData);
            response.put("total_pages", stocksPage.getTotalPages());
            response.put("total_elements", stocksPage.getTotalElements());

            grpcResponseHelper.sendJsonResponse("stocks_by_code", response, responseObserver);
        } catch (Exception e) {
            grpcResponseHelper.sendErrorResponse(e.getMessage(), responseObserver);
        }
    }
}
