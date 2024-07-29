package com.example.stockservice.service;

import com.example.common.MovingAverage;
import com.example.common.BollingerBands;
import com.example.common.MACD;
import com.example.common.RSI;
import com.example.stockservice.exception.kafka.*;
import com.example.stockservice.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

@Service
public class TechnicalIndicatorService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JsonUtil jsonUtil;

    public void calculateAndCacheIndicators(String symbol, String timeframe, List<Double> prices) {
        MovingAverage ma = new MovingAverage();
        BollingerBands bb = new BollingerBands();
        MACD macd = new MACD();
        RSI rsi = new RSI();

        ma.calculate(prices);
        bb.calculate(prices);
        macd.calculate(prices);
        rsi.calculate(prices);

        try {
            String sma12Json = jsonUtil.toJson(ma.getSma12());
            String sma20Json = jsonUtil.toJson(ma.getSma20());
            String sma26Json = jsonUtil.toJson(ma.getSma26());
            String bollingerBandsJson = jsonUtil.toJson(bb);
            String macdJson = jsonUtil.toJson(macd);
            String rsiJson = jsonUtil.toJson(rsi);

            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":sma12", sma12Json);
            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":sma20", sma20Json);
            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":sma26", sma26Json);
            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":bollingerBands", bollingerBandsJson);
            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":macd", macdJson);
            redisTemplate.opsForValue().set(symbol + ":" + timeframe + ":rsi", rsiJson);
        } catch (Exception e) {
            throw new RedisConnectionException(e);
        }
    }

    public List<Double> getSma12(String symbol, String timeframe) {
        try {
            String sma12Json = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":sma12");
            return jsonUtil.parseJson(sma12Json, List.class);
        } catch (Exception e) {
            throw new SMAException();
        }
    }

    public List<Double> getSma20(String symbol, String timeframe) {
        try {
            String sma20Json = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":sma20");
            return jsonUtil.parseJson(sma20Json, List.class);
        } catch (Exception e) {
            throw new SMAException();
        }
    }

    public List<Double> getSma26(String symbol, String timeframe) {
        try {
            String sma26Json = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":sma26");
            return jsonUtil.parseJson(sma26Json, List.class);
        } catch (Exception e) {
            throw new SMAException();
        }
    }

    public BollingerBands getBollingerBands(String symbol, String timeframe) {
        try {
            String bollingerBandsJson = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":bollingerBands");
            return jsonUtil.parseJson(bollingerBandsJson, BollingerBands.class);
        } catch (Exception e) {
            throw new BollingerBandsException();
        }
    }

    public MACD getMacd(String symbol, String timeframe) {
        try {
            String macdJson = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":macd");
            return jsonUtil.parseJson(macdJson, MACD.class);
        } catch (Exception e) {
            throw new MACDException();
        }
    }

    public RSI getRsi(String symbol, String timeframe) {
        try {
            String rsiJson = (String) redisTemplate.opsForValue().get(symbol + ":" + timeframe + ":rsi");
            return jsonUtil.parseJson(rsiJson, RSI.class);
        } catch (Exception e) {
            throw new RSIException();
        }
    }
}
