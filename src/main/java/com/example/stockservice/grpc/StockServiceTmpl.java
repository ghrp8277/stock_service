package com.example.stockservice.grpc;

import com.example.grpc.*;
import com.example.stockservice.annotation.GrpcExceptionHandler;
import com.example.stockservice.dto.*;
import com.example.common.Market;
import com.example.common.Stock;
import com.example.common.Favorite;
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
    private final String DEFAULT_RESULTS = "results";

    @Autowired
    private StockService stockService;

    @Autowired
    private GrpcResponseHelper grpcResponseHelper;

    @Override
    public void check(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setStatus(HealthCheckResponse.ServingStatus.SERVING)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @GrpcExceptionHandler
    public void getMarkets(Empty request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getStocksByMarket(GetStocksByMarketRequest request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getStockDataByMarketAndCode(GetStockDataByMarketAndCodeRequest request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getAllStocksByMarket(GetAllStocksByMarketRequest request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void searchStocksByName(SearchStocksByNameRequest request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void searchStocksByCode(SearchStocksByCodeRequest request, StreamObserver<Response> responseObserver) {
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
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getMovingAverages(GetMovingAveragesRequest request, StreamObserver<Response> responseObserver) {
        MovingAverageDto movingAverageDto = stockService.getMovingAverages(
                request.getStockCode(),
                request.getTimeframe(),
                request.getPeriodsList()
        );

        Map<String, Object> response = new HashMap<>();
        if (request.getPeriodsList().contains(12)) {
            response.put("sma12", movingAverageDto.getSma12());
        }
        if (request.getPeriodsList().contains(20)) {
            response.put("sma20", movingAverageDto.getSma20());
        }
        if (request.getPeriodsList().contains(26)) {
            response.put("sma26", movingAverageDto.getSma26());
        }
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getBollingerBands(GetBollingerBandsRequest request, StreamObserver<Response> responseObserver) {
        BollingerBandsDto bollingerBandsDto = stockService.getBollingerBands(request.getStockCode(), request.getTimeframe());
        Map<String, Object> response = new HashMap<>();
        response.put("upper_band", bollingerBandsDto.getUpperBand());
        response.put("middle_band", bollingerBandsDto.getMiddleBand());
        response.put("lower_band", bollingerBandsDto.getLowerBand());
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getMACD(GetMACDRequest request, StreamObserver<Response> responseObserver) {
        MACDDto macdDto = stockService.getMACD(request.getStockCode(), request.getTimeframe());
        Map<String, Object> response = new HashMap<>();
        response.put("macd_line", macdDto.getMacdLine());
        response.put("signal_line", macdDto.getSignalLine());
        response.put("histogram", macdDto.getHistogram());
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getRSI(GetRSIRequest request, StreamObserver<Response> responseObserver) {
        RSIDto rsiDto = stockService.getRSI(request.getStockCode(), request.getTimeframe());
        Map<String, Object> response = new HashMap<>();
        response.put("rsi", rsiDto.getRsi());
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void getFavoritesByUser(GetFavoritesByUserRequest request, StreamObserver<Response> responseObserver) {
        Page<Favorite> favorites = stockService.getFavoritesByUserId(
                request.getUserId(),
                request.getPage(),
                request.getPageSize()
        );
        List<Map<String, String>> favoriteList = favorites.stream()
                .map(fav -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("code", fav.getStock().getCode());
                    map.put("name", fav.getStock().getName());
                    map.put("market", fav.getStock().getMarket().getName());
                    return map;
                })
                .collect(Collectors.toList());
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("favorites", favoriteList);
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, responseMap, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void addFavorite(AddFavoriteRequest request, StreamObserver<Response> responseObserver) {
        Favorite favorite = stockService.addFavorite(request.getUserId(), request.getStockCode());
        Map<String, Object> response = new HashMap<>();
        response.put("code", favorite.getStock().getCode());
        response.put("name", favorite.getStock().getName());
        response.put("market", favorite.getStock().getMarket().getName());
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }

    @Override
    @GrpcExceptionHandler
    public void removeFavorite(RemoveFavoriteRequest request, StreamObserver<Response> responseObserver) {
        Favorite favorite = stockService.removeFavorite(request.getUserId(), request.getStockCode());
        Map<String, Object> response = new HashMap<>();
        response.put("code", favorite.getStock().getCode());
        response.put("name", favorite.getStock().getName());
        response.put("market", favorite.getStock().getMarket().getName());
        grpcResponseHelper.sendJsonResponse(DEFAULT_RESULTS, response, responseObserver);
    }
}
