package com.example.stockservice.service;

import com.example.stockservice.dto.*;
import com.example.stockservice.entity.Market;
import com.example.stockservice.entity.Stock;
import com.example.stockservice.entity.StockData;
import com.example.stockservice.entity.TechnicalIndicators.BollingerBands;
import com.example.stockservice.entity.TechnicalIndicators.MACD;
import com.example.stockservice.entity.TechnicalIndicators.MovingAverage;
import com.example.stockservice.entity.TechnicalIndicators.RSI;
import com.example.stockservice.repository.MarketRepository;
import com.example.stockservice.repository.StockDataRepository;
import com.example.stockservice.repository.StockRepository;
import com.example.stockservice.util.DateUtil;
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

@Service
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

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
    public MovingAverageDto getMovingAverages(String stockCode, String timeframe) {
        Optional<Stock> optionalStock = stockRepository.findByCode(stockCode);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            String startDate = DateUtil.calculateStartDate(timeframe);
            List<StockData> stockDataList = stockDataRepository.findByStockAndDateAfterWithMovingAverages(stock, startDate);

            List<Double> sma12 = stockDataList.stream()
                .flatMap(stockData -> stockData.getMovingAverage12().stream())
                .collect(Collectors.toList());

            List<Double> sma20 = stockDataList.stream()
                .flatMap(stockData -> stockData.getMovingAverage20().stream())
                .collect(Collectors.toList());

            List<Double> sma26 = stockDataList.stream()
                .flatMap(stockData -> stockData.getMovingAverage26().stream())
                .collect(Collectors.toList());

            return new MovingAverageDto(sma12, sma20, sma26);
        } else {
            throw new NoSuchElementException("Stock not found for code: " + stockCode);
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
            throw new NoSuchElementException("Stock not found for code: " + stockCode);
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
            throw new NoSuchElementException("Stock not found for code: " + stockCode);
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
            throw new NoSuchElementException("Stock not found for code: " + stockCode);
        }
    }
}
