package com.xavelo.crypto.adapter.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.xavelo.crypto.Price;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.time.Instant;
import java.math.BigDecimal;

import java.util.stream.Collectors; // {{ edit_1 }}

@Component
public class RedisAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisAdapter.class);

    private RedisTemplate<String, String> redisTemplate;
    
    public RedisAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;        
    }

    public void savePriceUpdate(Price price) {
        String hashKey = "coin:" + price.getCoin();
        redisTemplate.opsForHash().put(hashKey, "price:" + price.getTimestamp().toEpochMilli(), price.getPrice().toString());
        redisTemplate.opsForHash().put(hashKey, "currency:" + price.getTimestamp().toEpochMilli(), price.getCurrency());
    }

    public List<Price> getPriceUpdatesByCoin(String coin) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        List<Price> prices = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                Price price = new Price(coin, new BigDecimal(value), null, timestamp);
                prices.add(price);
            } else if (key.startsWith("currency:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                // You can store the currency in a separate data structure or in the same hash set
                // For simplicity, I'm ignoring the currency for now
            }
        }
        return prices;
    }

    public BigDecimal getLastPriceByCoin(String coin) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        BigDecimal lastPrice = null;
        Instant lastTimestamp = null;
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                if (lastTimestamp == null || timestamp.isAfter(lastTimestamp)) {
                    lastPrice = new BigDecimal(value);
                    lastTimestamp = timestamp;
                }
            }
        }
        return lastPrice;
    }

    public BigDecimal getAveragePriceByCoin(String coin) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        List<BigDecimal> prices = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                prices.add(new BigDecimal(value));
            }
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal price : prices) {
            sum = sum.add(price);
        }
        return sum.divide(new BigDecimal(prices.size()));
    }

    public double getAveragePriceForCoin(String coin) {
        String hashKey = "coin:" + coin;
        // Get all entries for the coin
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        List<Double> prices = new ArrayList<>();
        // Extract and parse price values
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("price:")) {
                String priceStr = (String) entry.getValue();
                prices.add(Double.parseDouble(priceStr));
            }
        }
        // Calculate average
        if (prices.isEmpty()) {
            return 0.0;
        }
        double sum = prices.stream().mapToDouble(Double::doubleValue).sum();
        return sum / prices.size();
    }

    public BigDecimal getAveragePriceByCoinInRange(String coin, int range, String unit) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        long now = System.currentTimeMillis() / 1000; // convert to seconds
        long startTime = getStartTime(now, range, unit);
    
        List<BigDecimal> prices = entries.entrySet().stream()
                .filter(entry -> ((String) entry.getKey()).startsWith("price:"))
                .filter(entry -> {
                    long timestamp = Long.parseLong(((String) entry.getKey()).replace("price:", ""));
                    return timestamp >= startTime && timestamp <= now;
                })
                .map(entry -> new BigDecimal((String) entry.getValue()))
                .collect(Collectors.toList());
    
        if (prices.isEmpty()) {
            // handle the case where no prices are found in the given time window
            // you can return a default value or throw an exception
            return BigDecimal.ZERO; // or throw new RuntimeException("No prices found in the given time window");
            return BigDecimal.ZERO; // or throw new RuntimeException("No prices found in the given time window");
        }
    
        BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(prices.size()));
    }
    
    private long getStartTime(long now, int range, String unit) {
        long startTime;
        switch (unit) {
            case "s":
                startTime = now - range;
                break;
            case "m":
                startTime = now - range * 60;
                break;
            case "h":
                startTime = now - range * 3600;
                break;
            case "d":
                startTime = now - range * 86400;
                break;
            default:
                throw new RuntimeException("Invalid unit: " + unit);
        }
        return startTime;
    }

}
