package com.example.stockservice.service;

import com.example.stockservice.constants.NaverSymbolConstants;
import com.example.stockservice.dto.StockDto;
import com.example.stockservice.entity.Market;
import com.example.stockservice.entity.Stock;
import com.example.stockservice.entity.StockData;
import com.example.stockservice.repository.MarketRepository;
import com.example.stockservice.repository.StockDataRepository;
import com.example.stockservice.repository.StockRepository;
import com.example.stockservice.util.DateUtil;
import com.example.stockservice.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
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

    public void collectAndSaveInitialData() {
        List<String> symbols = NaverSymbolConstants.ALL_SYMBOLS;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 10개의 쓰레드 사용

        for (String symbol : symbols) {
            executorService.submit(() -> {
                try {
                    List<StockDto> stockDataList = getStockDataWithRetry(symbol, NaverSymbolConstants.TimeFrame.DAY, 1250, 3);
                    saveStockData(stockDataList, symbol);
                } catch (Exception e) {
                    logger.error("Error processing symbol: " + symbol, e);
                }
            });
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void collectAndSaveDailyData() {
        List<String> symbols = NaverSymbolConstants.ALL_SYMBOLS;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 10개의 쓰레드 사용

        for (String symbol : symbols) {
            executorService.submit(() -> {
                try {
                    List<StockDto> stockDataList = getStockDataWithRetry(symbol, NaverSymbolConstants.TimeFrame.DAY, 1, 3); // 지난 하루 데이터 수집
                    saveStockData(stockDataList, symbol);
                } catch (Exception e) {
                    logger.error("Error processing symbol: " + symbol, e);
                }
            });
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("일간 주가 데이터를 수집하고 저장합니다.");
    }

    public List<StockDto> getStockDataWithRetry(String symbol, String timeframe, int count, int maxRetries) throws Exception {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return getStockData(symbol, timeframe, count);
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e;
                }
                logger.warn("Retrying getStockData for symbol: " + symbol + ", attempt: " + attempt);
                Thread.sleep(1000); // 재시도 전 대기 시간
            }
        }
        return Collections.emptyList(); // 여기에 도달하지 않아야 함
    }

    public List<StockDto> getStockData(String symbol, String timeframe, int count) throws Exception {
        String url = String.format("%s?symbol=%s&timeframe=%s&count=%d&requestType=0",
                NaverSymbolConstants.BASE_URL, symbol, timeframe, count);

        String response = HttpUtil.sendGetRequest(url);
        return parseXml(response);
    }

    private List<StockDto> parseXml(String xml) throws Exception {
        List<StockDto> stockDataList = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document document = builder.parse(is);

        NodeList items = document.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String data = item.getAttribute("data");
            String[] values = data.split("\\|");

            StockDto stockData = new StockDto(
                    values[0],
                    Integer.parseInt(values[1]),
                    Integer.parseInt(values[2]),
                    Integer.parseInt(values[3]),
                    Integer.parseInt(values[4]),
                    Integer.parseInt(values[5])
            );
            stockDataList.add(stockData);
        }

        return stockDataList;
    }

    private void saveStockData(List<StockDto> stockDataList, String symbol) {
        Optional<Stock> optionalStock = stockRepository.findByCode(symbol);

        if (optionalStock.isEmpty()) {
            System.err.println("Stock with code " + symbol + " not found in the database.");
            return;
        }

        Stock stock = optionalStock.get();

        List<StockData> existingStockDataList = stockDataRepository.findByStock(stock);
        Map<String, StockData> existingStockDataMap = new HashMap<>();
        for (StockData existingStockData : existingStockDataList) {
            existingStockDataMap.put(existingStockData.getDate(), existingStockData);
        }

        List<StockData> stockDataEntities = new ArrayList<>();
        for (StockDto stockDto : stockDataList) {
            if (!existingStockDataMap.containsKey(stockDto.getDate())) {
                StockData stockData = new StockData();
                stockData.setDate(stockDto.getDate());
                stockData.setOpenPrice(stockDto.getOpenPrice());
                stockData.setHighPrice(stockDto.getHighPrice());
                stockData.setLowPrice(stockDto.getLowPrice());
                stockData.setClosePrice(stockDto.getClosePrice());
                stockData.setVolume(stockDto.getVolume());
                stockData.setStock(stock);

                stockDataEntities.add(stockData);
            }
        }

        if (!stockDataEntities.isEmpty()) {
            stockDataRepository.saveAll(stockDataEntities);
        }
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

    public List<Stock> searchStocksByName(String name) {
        return stockRepository.findByNameContaining(name);
    }

    public List<Stock> searchStocksByCode(String code) {
        return stockRepository.findAllByCode(code);
    }

    public List<Stock> searchStocksByMarket(String market) {
        return stockRepository.findByMarketName(market);
    }
}
