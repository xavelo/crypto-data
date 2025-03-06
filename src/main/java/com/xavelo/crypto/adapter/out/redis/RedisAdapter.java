package com.xavelo.crypto.adapter.out.redis;

import com.xavelo.crypto.domain.model.CoinData;
import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.model.serdes.CoinDataSerializer;
import com.xavelo.crypto.domain.repository.CoinDataRepository;
import com.xavelo.crypto.domain.repository.PriceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.xavelo.crypto.domain.model.serdes.PriceSerializer.deserializePrice;


@Component
@AllArgsConstructor
public class RedisAdapter implements PriceRepository, CoinDataRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisAdapter.class);

    private RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate; // Needed for ZSET operations

    private final MeterRegistry meterRegistry;

    @Override
    public long countPriceUpdates(String coin) {
        String zsetKey = "prices:" + coin;
        return Optional.ofNullable(stringRedisTemplate.opsForZSet().zCard(zsetKey)).orElse(0L);
    }

    @Override
    public Price getLatestPrice(String coin) {
        logger.info("-> getLatestPrice {}", coin);
        String key = "last_price:" + coin;
        String json = redisTemplate.opsForValue().get(key);
        logger.info("<- getLatestPrice {}", json);
        return json != null ? deserializePrice(json) : null;
    }

    @Override
    public void saveCoinData(CoinData coinData) {
        long startTime = System.nanoTime();
        String key = "coin_data:" + coinData.getSymbol();
        String json = CoinDataSerializer.serializeCoinData(coinData);
        redisTemplate.opsForValue().set(key, json);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        logger.debug("coin.data.save.redis.time: {}ms", processingTime);
    }

    @Override
    public  CoinData getCoinData(String coin) {
        long startTime = System.nanoTime();
        String key = "coin_data:" + coin;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
           return null; // Or throw an exception if needed
        }
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        logger.debug("coin.data.get.redis.time: {}ms", processingTime);
        return CoinDataSerializer.deserializeCoinData(json);
    }

}