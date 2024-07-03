package com.example.stockservice.service;

import com.example.stockservice.constants.NaverSymbolConstants;
import com.example.stockservice.dto.StockDto;
import com.example.stockservice.util.HttpUtil;
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

@Service
public class StockService {
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
}
