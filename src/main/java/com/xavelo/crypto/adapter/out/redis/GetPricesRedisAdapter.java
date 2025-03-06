package com.xavelo.crypto.adapter.out.redis;

import com.xavelo.crypto.application.port.out.GetPricesPort;
import com.xavelo.crypto.domain.model.Price;
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
public class GetPricesRedisAdapter implements GetPricesPort {
    
    private static final Logger logger = LoggerFactory.getLogger(GetPricesRedisAdapter.class);

    private RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate; // Needed for ZSET operations

    private final MeterRegistry meterRegistry;

    @Override
    public Price getLastPrice(String coin) {
        logger.info("-> getLatestPrice {}", coin);
        String key = "last_price:" + coin;
        String json = redisTemplate.opsForValue().get(key);
        logger.info("<- getLatestPrice {}", json);
        return json != null ? deserializePrice(json) : null;
    }

    @Override
    public long countPriceUpdates() {
        return 999;
    }

    @Override
    public long countPriceUpdates(String coin) {
        String zsetKey = "prices:" + coin;
        return Optional.ofNullable(stringRedisTemplate.opsForZSet().zCard(zsetKey)).orElse(0L);
    }

}