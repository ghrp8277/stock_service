package com.example.stockservice.service;

import com.example.stockservice.constants.TimeframeConstants;
import com.example.stockservice.dto.*;
import com.example.common.Market;
import com.example.common.Stock;
import com.example.common.StockData;
import com.example.common.BollingerBands;
import com.example.common.MACD;
import com.example.common.RSI;
import com.example.stockservice.repository.MarketRepository;
import com.example.stockservice.repository.StockDataRepository;
import com.example.stockservice.repository.StockRepository;
import com.example.stockservice.util.DateUtil;
import jakarta.persistence.Tuple;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import com.example.stockservice.exception.*;

@Service
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private TechnicalIndicatorService technicalIndicatorService;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    public StockDto convertToDto(Tuple tuple) {
        return new StockDto(
            tuple.get("date", String.class),
            tuple.get("openPrice", Integer.class),
            tuple.get("highPrice", Integer.class),
            tuple.get("lowPrice", Integer.class),
            tuple.get("closePrice", Integer.class),
            tuple.get("volume", Integer.class)
        );
    }

    public List<StockDto> convertToDtoList(List<Tuple> tuples) {
        return tuples.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<Market> getMarkets() {
        return marketRepository.findAll();
    }

    public List<Stock> getStocksByMarket(String marketName) {
        Optional<Market> optionalMarket = marketRepository.findByName(marketName);
        if (optionalMarket.isPresent()) {
            Market market = optionalMarket.get();
            return stockRepository.findByMarket(market);
        } else {
            return new ArrayList<>();
        }
    }

    @Cacheable("initialData")
    public InitialStockDto getInitialStockData(String marketName, String code, String timeframe) {
        List<StockDto> stockData = getStockDataByMarketAndCode(marketName, code, timeframe);

        List<Double> closePrices = stockData.stream()
                .map(StockDto::getClosePrice)
                .map(Integer::doubleValue)
                .collect(Collectors.toList());

        String symbol = marketName + ":" + code;

        try {
            technicalIndicatorService.calculateAndCacheIndicators(symbol, timeframe, closePrices);
        } catch (Exception e) {
            throw e;
        }

        MovingAverageDto movingAverages = new MovingAverageDto(
                technicalIndicatorService.getSma12(symbol, timeframe),
                technicalIndicatorService.getSma20(symbol, timeframe),
                technicalIndicatorService.getSma26(symbol, timeframe)
        );

        BollingerBands bb = technicalIndicatorService.getBollingerBands(symbol, timeframe);
        BollingerBandsDto bollingerBands = new BollingerBandsDto(bb.getUpperBand(), bb.getMiddleBand(), bb.getLowerBand());

        MACD macdValue = technicalIndicatorService.getMacd(symbol, timeframe);
        MACDDto macd = new MACDDto(macdValue.getMacdLine(), macdValue.getSignalLine(), macdValue.getHistogram());

        RSI rsiValue = technicalIndicatorService.getRsi(symbol, timeframe);
        RSIDto rsi = new RSIDto(rsiValue.getRsi());

        return new InitialStockDto(marketName, code, stockData, movingAverages, bollingerBands, macd, rsi);
    }

    public DailyStockDataDto getDailyStockData(String code) {
        Stock stock = stockRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Stock not found with code: " + code));
        String marketName = stock.getMarket().getName();
        Pageable pageable = PageRequest.of(0, 30);
        List<Tuple> stockDataTuples = stockDataRepository.findPast30DaysByMarketAndCode(marketName, code, pageable).getContent();
        List<StockDto> stockDtoList = this.convertToDtoList(stockDataTuples);
        return processAndSendDailyData(marketName, code, stockDtoList);
    }

    public DailyStockDataDto processAndSendDailyData(String marketName, String code, List<StockDto> stockDataList) {
        String symbol = marketName + ":" + code;

        List<Double> closePrices = stockDataList.stream()
                .map(StockDto::getClosePrice)
                .map(Integer::doubleValue)
                .collect(Collectors.toList());

        try {
            technicalIndicatorService.calculateAndCacheIndicators(symbol, TimeframeConstants.ONE_DAY, closePrices);
        } catch (Exception e) {
            throw e;
        }

        List<Double> sma12 = technicalIndicatorService.getSma12(symbol, TimeframeConstants.ONE_DAY);
        List<Double> sma20 = technicalIndicatorService.getSma20(symbol, TimeframeConstants.ONE_DAY);
        List<Double> sma26 = technicalIndicatorService.getSma26(symbol, TimeframeConstants.ONE_DAY);

        MovingAverageDto movingAverages = new MovingAverageDto(
                Collections.singletonList(sma12.getFirst()),
                Collections.singletonList(sma20.getFirst()),
                Collections.singletonList(sma26.getFirst())
        );

        BollingerBands bb = technicalIndicatorService.getBollingerBands(symbol, TimeframeConstants.ONE_DAY);
        BollingerBandsDto bollingerBands = new BollingerBandsDto(
                Collections.singletonList(bb.getUpperBand().getFirst()),
                Collections.singletonList(bb.getMiddleBand().getFirst()),
                Collections.singletonList(bb.getLowerBand().getFirst())
        );

        MACD macdValue = technicalIndicatorService.getMacd(symbol, TimeframeConstants.ONE_DAY);
        MACDDto macd = new MACDDto(
                Collections.singletonList(macdValue.getMacdLine().getFirst()),
                Collections.singletonList(macdValue.getSignalLine().getFirst()),
                Collections.singletonList(macdValue.getHistogram().getFirst())
        );

        RSI rsiValue = technicalIndicatorService.getRsi(symbol, TimeframeConstants.ONE_DAY);
        RSIDto rsi = new RSIDto(Collections.singletonList(rsiValue.getRsi().getFirst()));

        if (stockDataList.isEmpty()) {
            throw new RuntimeException("Stock data list is empty");
        }

        StockDto latestStockData = stockDataList.getFirst();

        return new DailyStockDataDto(
                marketName,
                code,
                Collections.singletonList(latestStockData),
                movingAverages,
                bollingerBands,
                macd,
                rsi
        );
    }

    @Cacheable("stockData")
    public List<StockDto> getStockDataByMarketAndCode(String marketName, String code, String timeframe) {
        Optional<Stock> optionalStock = stockRepository.findByCodeAndMarketName(code, marketName);

        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndStock_MarketNameAndDateAfter(stock, marketName, startDate);
            return stockDataList.stream().map(stockData -> new StockDto(
                    stockData.getDate(),
                    stockData.getOpenPrice(),
                    stockData.getHighPrice(),
                    stockData.getLowPrice(),
                    stockData.getClosePrice(),
                    stockData.getVolume()
            )).collect(Collectors.toList());
        }
        return List.of();
    }

    public Page<Stock> getAllStocksByMarket(String marketName, int page, int size, String[] sort) {
        Optional<Market> optionalMarket = marketRepository.findByName(marketName);

        if (optionalMarket.isPresent()) {
            Market market = optionalMarket.get();
            List<Sort.Order> orders = new ArrayList<>();

            if (sort[0].contains(",")) {
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
                }
            } else {
                orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
            }

            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            return stockRepository.findByMarket(market, pagingSort);
        } else {
            return Page.empty();
        }
    }

     public Page<Stock> searchStocksByName(String name, int page, int size, String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
        }

        Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
        return stockRepository.findByNameContaining(name, pagingSort);
    }

    public Page<Stock> searchStocksByCode(String code, int page, int size, String[] sort) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
        }

        Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
        return stockRepository.findAllByCodeContaining(code, pagingSort);
    }

    @Cacheable("movingAverages")
    public MovingAverageDto getMovingAverages(String stockCode, String timeframe, List<Integer> periods) {
        Optional<Stock> optionalStock = stockRepository.findByCode(stockCode);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndDateAfterWithMovingAverages(stock, startDate);

            List<Double> sma12 = null;
            List<Double> sma20 = null;
            List<Double> sma26 = null;

            if (periods.contains(12)) {
                sma12 = stockDataList.stream()
                        .flatMap(stockData -> stockData.getMovingAverage12().stream())
                        .collect(Collectors.toList());
            }

            if (periods.contains(20)) {
                sma20 = stockDataList.stream()
                        .flatMap(stockData -> stockData.getMovingAverage20().stream())
                        .collect(Collectors.toList());
            }

            if (periods.contains(26)) {
                sma26 = stockDataList.stream()
                        .flatMap(stockData -> stockData.getMovingAverage26().stream())
                        .collect(Collectors.toList());
            }

            return new MovingAverageDto(sma12, sma20, sma26);
        } else {
            throw new StockNotFoundException();
        }
    }

    @Cacheable("bollingerBands")
    public BollingerBandsDto getBollingerBands(String stockCode, String timeframe) {
        Optional<Stock> optionalStock = stockRepository.findByCode(stockCode);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndDateAfter(stock, startDate);

            for (StockData stockData : stockDataList) {
                Hibernate.initialize(stockData.getBollingerBands().getUpperBand());
                Hibernate.initialize(stockData.getBollingerBands().getMiddleBand());
                Hibernate.initialize(stockData.getBollingerBands().getLowerBand());
            }

            List<Double> upperBand = stockDataList.stream()
                .flatMap(stockData -> stockData.getBollingerBands().getUpperBand().stream())
                .collect(Collectors.toList());

            List<Double> middleBand = stockDataList.stream()
                .flatMap(stockData -> stockData.getBollingerBands().getMiddleBand().stream())
                .collect(Collectors.toList());

            List<Double> lowerBand = stockDataList.stream()
                .flatMap(stockData -> stockData.getBollingerBands().getLowerBand().stream())
                .collect(Collectors.toList());

            return new BollingerBandsDto(upperBand, middleBand, lowerBand);
        } else {
            throw new StockNotFoundException();
        }
    }

    @Cacheable("macd")
    public MACDDto getMACD(String stockCode, String timeframe) {
        Optional<Stock> optionalStock = stockRepository.findByCode(stockCode);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndDateAfter(stock, startDate);

            for (StockData stockData : stockDataList) {
                Hibernate.initialize(stockData.getMacd().getMacdLine());
                Hibernate.initialize(stockData.getMacd().getSignalLine());
                Hibernate.initialize(stockData.getMacd().getHistogram());
            }

            List<Double> macdLine = stockDataList.stream()
                .flatMap(stockData -> stockData.getMacd().getMacdLine().stream())
                .collect(Collectors.toList());

            List<Double> signalLine = stockDataList.stream()
                .flatMap(stockData -> stockData.getMacd().getSignalLine().stream())
                .collect(Collectors.toList());

            List<Double> histogram = stockDataList.stream()
                .flatMap(stockData -> stockData.getMacd().getHistogram().stream())
                .collect(Collectors.toList());

            return new MACDDto(macdLine, signalLine, histogram);
        } else {
            throw new StockNotFoundException();
        }
    }

    @Cacheable("rsi")
    public RSIDto getRSI(String stockCode, String timeframe) {
        Optional<Stock> optionalStock = stockRepository.findByCode(stockCode);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndDateAfter(stock, startDate);

            for (StockData stockData : stockDataList) {
                Hibernate.initialize(stockData.getRsi());
            }

            List<Double> rsi = stockDataList.stream()
                .map(StockData::getRsi)
                .collect(Collectors.toList());

            return new RSIDto(rsi);
        } else {
            throw new StockNotFoundException();
        }
    }
}
