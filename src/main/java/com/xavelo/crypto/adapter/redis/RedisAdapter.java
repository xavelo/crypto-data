package com.xavelo.crypto.adapter.redis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.xavelo.crypto.service.PriceService;
import com.xavelo.crypto.model.Price;

import java.util.HashMap; // {{ edit_1 }}

@Component
public class RedisAdapter implements PriceService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisAdapter.class);

    private RedisTemplate<String, String> redisTemplate;
    private final MeterRegistry meterRegistry;
    
    public RedisAdapter(RedisTemplate<String, String> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;   
        this.meterRegistry = meterRegistry;     
    }

    @Override
    public void savePriceUpdate(Price price) {
        long startTime = System.nanoTime();
        String hashKey = "coin:" + price.getCoin();
        redisTemplate.opsForHash().put(hashKey, "price:" + price.getTimestamp().toEpochMilli(), price.getPrice().toString());
        redisTemplate.opsForHash().put(hashKey, "currency:" + price.getTimestamp().toEpochMilli(), price.getCurrency());
        redisTemplate.opsForValue().set("last_price:" + price.getCoin(), price.getPrice().toString() + ":" + price.getTimestamp().toEpochMilli() + ":" + price.getCurrency());
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        //logger.info("crypto.price.save.redis.time: [{}ms]", processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.save.redis.time")
                .description("Time taken to save crypto price updates to redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);    
    }


    @Override
    public long getPriceUpdatesCount() {
        String pattern = "coin:*";
        Set<String> keys = redisTemplate.keys(pattern);
        long count = 0;
        for (String key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            // Count only fields that start with "price:"
            count += entries.keySet().stream()
                    .filter(k -> ((String) k).startsWith("price:"))
                    .count();
        }
        return count;
    }

    @Override
    public long getPriceUpdatesCountByCoin(String coin) {
        String pattern = "coin:" + coin; // {{ edit_1 }}
        Set<String> keys = redisTemplate.keys(pattern);
        long count = 0;
        for (String key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            // Count only fields that start with "price:"
            count += entries.keySet().stream()
                    .filter(k -> ((String) k).startsWith("price:"))
                    .count();
        }
        return count;
    }

    @Override
    public long getPriceUpdatesCountByCoinInRange(String coin, int range, String unit) {
        String pattern = "coin:" + coin; // {{ edit_1 }}
        Set<String> keys = redisTemplate.keys(pattern);
        long count = 0;
        long now = System.currentTimeMillis();
        long startTimeInRange = getStartTime(now, range, unit); // Calculate start time based on unit

        for (String key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            // Count only fields that start with "price:" and are within the time range
            count += entries.keySet().stream()
                    .filter(k -> ((String) k).startsWith("price:"))
                    .filter(k -> {
                        long timestamp = Long.parseLong(((String) k).split(":")[1]);
                        return timestamp >= startTimeInRange && timestamp <= now; // Check if within range
                    })
                    .count();
        }
        return count;
    }


    @Override
    public Price getLastPriceByCoin(String coin) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        BigDecimal lastPrice = null;
        Instant lastTimestamp = null;
        String lastCurrency = null;
        
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                long epochMilli = Long.parseLong(key.split(":")[1]);
                Instant timestamp = Instant.ofEpochMilli(epochMilli); 
                if (lastTimestamp == null || timestamp.isAfter(lastTimestamp)) {
                    lastPrice = new BigDecimal(value);
                    lastTimestamp = timestamp;
                }
            } else if (key.startsWith("currency:")) {
                lastCurrency = (String) value;
            }
        }
        return new Price(coin, lastPrice, lastCurrency, lastTimestamp); 
    }

    @Override
    public Price getHistoricalPriceByCoin(String coin, int range, String unit) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        
        Instant now = Instant.now();
        Instant targetTimestamp = Instant.ofEpochMilli(getStartTime(System.currentTimeMillis(), range, unit));
        
        // Log the targetTimestamp for debugging
        logger.info("targetTimestamp for coin {}: {} - now {}", coin, targetTimestamp, now);
        
        Price historicalPrice = null;
        long margin = 30 * 1000; // 30 seconds in milliseconds

        // Single pass to find the closest historical price within the margin
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                // Check if the timestamp is within the range with a 30 seconds margin
                if (timestamp.compareTo(now) <= 0 && timestamp.compareTo(targetTimestamp.minusMillis(margin)) >= 0 && 
                    (historicalPrice == null || 
                    Duration.between(targetTimestamp, timestamp).abs().compareTo(Duration.between(targetTimestamp, historicalPrice.getTimestamp())) < 0)) { 
                    historicalPrice = new Price(coin, new BigDecimal(value), null, timestamp);
                }
            }
        }
        logger.info("historicalPrice: {}", historicalPrice);
        return historicalPrice;
    }

    @Override
    public BigDecimal getAveragePriceByCoinInRange(String coin, int range, String unit) {    
        long startTime = System.nanoTime();    
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        long now = System.currentTimeMillis();
        long startTimeInRange = getStartTime(now, range, unit); // Calculate start time based on unit
        List<BigDecimal> prices = entries.entrySet().stream()
                .filter(entry -> ((String) entry.getKey()).startsWith("price:"))
                .filter(entry -> {
                    long timestamp = Long.parseLong(((String) entry.getKey()).replace("price:", ""));
                    return timestamp >= startTimeInRange && timestamp <= now; // Use calculated start time
                })
                .map(entry -> new BigDecimal((String) entry.getValue()))
                .collect(Collectors.toList());
    
        if (prices.isEmpty()) {
            return BigDecimal.ZERO; // or throw new RuntimeException("No prices found in the given time window");
        }
    
        BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(new BigDecimal(prices.size()));
        logger.info("getAveragePriceByCoinInRange {} {} {} = {}", coin, range, unit, average);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        logger.info("crypto.price.calc.average.coin.range.redis.time for {}: {} [{}ms]", coin, average, processingTime);

        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.calc.average.coin.range.redis.time")
        .description("Time taken to calculate average price in a given time range for a given coin")
        .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        return average;
    }
    
    private long getStartTime(long now, int range, String unit) {
        long startTime;
        switch (unit) {
            case "s":
                startTime = now - range * 1000; // Multiply by 1000 for seconds
                break;
            case "m":
                startTime = now - range * 60 * 1000; // Multiply by 60, then by 1000 for minutes
                break;
            case "h":
                startTime = now - range * 3600 * 1000; // Multiply by 3600, then by 1000 for hours
                break;
            case "d":
                startTime = now - range * 86400 * 1000; // Multiply by 86400, then by 1000 for days
                break;
            default:
                throw new RuntimeException("Invalid unit: " + unit);
        }
        return startTime;
    }   

    @Override
    public List<Price> getPriceUpdatesByCoin(String coin) {
        String hashKey = "coin:" + coin;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
        List<Price> prices = new ArrayList<>();
        Map<Long, String> currencyMap = new HashMap<>(); // {{ edit_1 }}

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.startsWith("price:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                prices.add(new Price(coin, new BigDecimal(value), currencyMap.getOrDefault(timestamp.toEpochMilli(), null), timestamp)); // {{ edit_2 }}
            } else if (key.startsWith("currency:")) {
                Instant timestamp = Instant.ofEpochMilli(Long.parseLong(key.split(":")[1]));
                currencyMap.put(timestamp.toEpochMilli(), (String) value); // {{ edit_3 }}
            }
        }
        return prices;
    }

}