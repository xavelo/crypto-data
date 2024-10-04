package com.xavelo.crypto.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.Price;
import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.adapter.mongo.PriceRepository;

import java.util.concurrent.TimeUnit;

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

    private final PriceRepository repository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    public CryptoPriceUpdatesListener(PriceRepository repository, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException, InterruptedException {
        logger.info("Received message: key {} - value {}", key, message);

        // Start timer
        long startTime = System.nanoTime();
        
        // Simulate processing time
        Thread.sleep(5000); // Delay for 5 seconds

        Price price = objectMapper.readValue(message, Price.class);
        PriceDocument.PriceId priceId = new PriceDocument.PriceId(price.getCoin(), price.getTimestamp());
        PriceDocument document = new PriceDocument(
            priceId,
            price.getPrice(),
            price.getCurrency()
        );
        repository.save(document);

        // End timer
        long endTime = System.nanoTime();
        
        // Calculate processing time in milliseconds
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.processing.time")
                .description("Time taken to process crypto price updates")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);

    }

}

