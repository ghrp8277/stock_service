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
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {
    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    public void collectAndSaveInitialData() {
        List<String> symbols = NaverSymbolConstants.ALL_SYMBOLS;

        for (String symbol : symbols) {
            try {
                List<StockDto> stockDataList = getStockData(symbol, NaverSymbolConstants.TimeFrame.DAY, 1250);
                saveStockData(stockDataList, symbol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 일간 데이터 수집 메서드
    public void collectAndSaveDailyData() {
        List<String> symbols = NaverSymbolConstants.ALL_SYMBOLS;

        for (String symbol : symbols) {
            try {
                List<StockDto> stockDataList = getStockData(symbol, NaverSymbolConstants.TimeFrame.DAY, 1); // 지난 하루 데이터 수집
                saveStockData(stockDataList, symbol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 네이버 주가 API를 호출하여 일간 데이터를 수집하고 DB에 저장하는 로직
        System.out.println("일간 주가 데이터를 수집하고 저장합니다.");
    }

    // 실시간 데이터 수집 메서드
    public void collectAndSaveRealTimeData() {
        List<String> symbols = NaverSymbolConstants.ALL_SYMBOLS;

        for (String symbol : symbols) {
            try {
                List<StockDto> stockDataList = getStockData(symbol, NaverSymbolConstants.TimeFrame.MINUTE, 1); // 실시간 데이터 수집
                saveStockData(stockDataList, symbol);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("실시간 주가 데이터를 수집하고 저장합니다.");
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

        for (StockDto stockDto : stockDataList) {
            Optional<StockData> existingStockData = stockDataRepository.findByDateAndStock(stockDto.getDate(), stock);
            if (existingStockData.isEmpty()) {
                StockData stockData = new StockData();
                stockData.setDate(stockDto.getDate());
                stockData.setOpenPrice(stockDto.getOpenPrice());
                stockData.setHighPrice(stockDto.getHighPrice());
                stockData.setLowPrice(stockDto.getLowPrice());
                stockData.setClosePrice(stockDto.getClosePrice());
                stockData.setVolume(stockDto.getVolume());
                stockData.setStock(stock);

                stockDataRepository.save(stockData);
            }
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
