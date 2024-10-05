package com.xavelo.crypto.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.Price;
import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.adapter.mongo.PriceRepository;
import com.xavelo.crypto.adapter.redis.RedisAdapter;
import com.xavelo.crypto.service.PriceService;

import java.util.concurrent.TimeUnit;
import java.math.BigDecimal; // {{ edit_1 }}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private final PriceService priceService;
    private final PriceRepository repository;
    private final RedisAdapter redisAdapter;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public CryptoPriceUpdatesListener(PriceService priceService, 
                                      PriceRepository repository, 
                                      RedisAdapter redisAdapter, 
                                      ObjectMapper objectMapper, 
                                      MeterRegistry meterRegistry) {
        this.priceService = priceService;
        this.repository = repository;
        this.redisAdapter = redisAdapter;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException, InterruptedException {
        logger.info("Received message: key {} - value {}", key, message);

        // Start timer
        long startTime = System.nanoTime();

        Price price = objectMapper.readValue(message, Price.class);
        saveToMongo(price);
        saveToRedis(price);
  
        priceService.getAveragePriceByCoin(price.getCoin());
        priceService.getAveragePriceByCoinInRange(price.getCoin(), 5, "m");

        // End timer
        long endTime = System.nanoTime();
        
        // Calculate processing time in milliseconds
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        logger.info("crypto.price.processing.time: {}ms", processingTime);
        
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.processing.time")
                .description("Time taken to process crypto price updates")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);

    }

    private void saveToMongo(Price price) {
        long startTime = System.nanoTime();
        PriceDocument.PriceId priceId = new PriceDocument.PriceId(price.getCoin(), price.getTimestamp());
        PriceDocument document = new PriceDocument(
            priceId,
            price.getPrice(),
            price.getCurrency()
        );
        repository.save(document);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.save.mongo.time: {}ms", processingTime);
        
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.save.mongo.time")
                .description("Time taken to save crypto price updates to mongo")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
    }

    /*
    private void saveToRedis(Price price) {
        long startTime = System.nanoTime();
        redisAdapter.savePriceUpdate(price);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.save.redis.time: {}ms", processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.save.redis.time")
                .description("Time taken to save crypto price updates to redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
    }*/

    /*
    private BigDecimal getAveragePrice(String coin) {
        long startTime = System.nanoTime();
        BigDecimal averagePrice = redisAdapter.getAveragePriceByCoin(coin);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.calc.average.coin.redis.time for {}: {}ms", coin, processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.calc.average.coin.redis.time")
                .description("Time taken to calculate average price from redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        logger.info("{} average price: {}", coin, averagePrice);
        return averagePrice;
    }*/

    
    private BigDecimal getLastPrice(String coin) {
        long startTime = System.nanoTime();
        BigDecimal lastPrice = redisAdapter.getLastPriceByCoin(coin);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.calc.last.coin.redis.time for {}: {}ms", coin, processingTime);
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.calc.last.coin.redis.time")
                .description("Time taken to calculate last price from redis")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        logger.info("{} last price: {}", coin, lastPrice);
        return lastPrice;
    }

}

