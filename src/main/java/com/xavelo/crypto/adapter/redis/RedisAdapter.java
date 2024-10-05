package com.xavelo.crypto.adapter.redis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.xavelo.crypto.service.PriceService;
import com.xavelo.crypto.Price;

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
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.save.redis.time: {}ms", processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.save.redis.time")
                .description("Time taken to save crypto price updates to redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
    }

    @Override
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

    @Override
    public BigDecimal getAveragePriceByCoin(String coin) {
        long startTime = System.nanoTime();
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
        // Handle case where no prices are found to avoid divide by zero
        if (prices.size() == 0) {
            return BigDecimal.ZERO;
        }
        // Set the scale to 8 decimal places and rounding mode
        BigDecimal average = sum.divide(new BigDecimal(prices.size()), 8, RoundingMode.HALF_UP);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.calc.average.coin.redis.time for {}: {} [{}ms]", coin, average, processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.calc.average.coin.redis.time")
                .description("Time taken to calculate average price for a given coin")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        return average;
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

}
