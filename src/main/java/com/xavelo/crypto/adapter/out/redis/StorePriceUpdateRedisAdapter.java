package com.xavelo.crypto.adapter.out.redis;

import com.xavelo.crypto.application.port.out.StorePriceUpdatePort;
import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.model.serdes.PriceSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class StorePriceUpdateRedisAdapter implements StorePriceUpdatePort {
    
    private static final Logger logger = LoggerFactory.getLogger(StorePriceUpdateRedisAdapter.class);

    private RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate; // Needed for ZSET operations

    private final MeterRegistry meterRegistry;

    @Override
    public void storePriceUpdate(Price price) {
        long startTime = System.nanoTime();

        String zsetKey = "prices:" + price.getCoin();
        String latestKey = "last_price:" + price.getCoin();

        // Store the price in a sorted set (timestamp as score)
        String jsonValue = PriceSerializer.serializePrice(price);
        stringRedisTemplate.opsForZSet().add(zsetKey, jsonValue, price.getTimestamp().getTime());

        // Store the latest price separately
        redisTemplate.opsForValue().set(latestKey, jsonValue);

        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        logger.debug("crypto.price.save.redis.time: {}ms", processingTime);

        // Send metric to metrics server
        /*
        Timer timer = Timer.builder("crypto.price.save.redis.time")
                .description("Time taken to save crypto price updates to redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        */
    }

}