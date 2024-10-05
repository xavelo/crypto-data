package com.xavelo.crypto.adapter.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.xavelo.crypto.Price;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.time.Instant;
import java.math.BigDecimal;




@Component
public class RedisAdapter {
    
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

}
