package com.xavelo.crypto.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.adapter.influxdb.InfluxDBAdapter;
import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.adapter.mongo.PriceRepository;
import com.xavelo.crypto.service.PriceService;
import com.xavelo.crypto.model.Price;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

@Service
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private final PriceService priceService;
    private final PriceRepository mongoPricerepository;
    private final InfluxDBAdapter influxDBAdapter;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Counter retryCounter; // Add a counter for retries

    private static final int MAX_RETRIES = 3; 
    
    public CryptoPriceUpdatesListener(PriceService priceService, 
                                      PriceRepository mongoPricerepository,
                                      InfluxDBAdapter influxDBAdapter,                                   
                                      ObjectMapper objectMapper, 
                                      MeterRegistry meterRegistry,
                                      KafkaTemplate<String, String> kafkaTemplate) {
        this.priceService = priceService;
        this.mongoPricerepository = mongoPricerepository;
        this.influxDBAdapter = influxDBAdapter;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
        this.retryCounter = Counter.builder("crypto.price.processing.retries") // Initialize the counter
                .description("Count of retries for processing crypto price updates")
                .register(meterRegistry);
    }

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException, InterruptedException {
        logger.info("Received message: key {} - value {}", key, message);
        process(message);                
    }

    private void process(String message) {

        // test
        /*
        if(message.contains("XRP")) {
            logger.info("testing error for XRP price...");
            message = "test error";
        }*/

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                long startTime = System.nanoTime();

                Price price = objectMapper.readValue(message, Price.class);
                saveToMongo(price);
                priceService.savePriceUpdate(price);
                influxDBAdapter.writePriceUpdate(price);

                long processingTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
                logger.info("crypto.price.processing.time: {}ms", processingTime);

                Timer timer = Timer.builder("crypto.price.processing.time")
                        .description("Time taken to process crypto price updates")
                        .register(meterRegistry);
                timer.record(processingTime, TimeUnit.MILLISECONDS);
                
                return;

            } catch (JsonProcessingException e) {
                attempt++;
                retryCounter.increment(); // Increment the retry counter
                logger.error("Attempt {}: error {} processing message {}", attempt, e.getMessage(), message);
                if (attempt >= MAX_RETRIES) {
                    sendToDLQ(message); // Send to DLQ after max retries
                } else {
                    // Exponential backoff delay
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000); // Delay in milliseconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }
            }
        }

    }

    // New method to send message to DLQ
    private void sendToDLQ(String message) {       
        kafkaTemplate.send("crypto-price-updates-topic-dlq", message);
    }

    private void saveToMongo(Price price) {
        long startTime = System.nanoTime();
        PriceDocument.PriceId priceId = new PriceDocument.PriceId(price.getCoin(), price.getTimestamp());
        PriceDocument document = new PriceDocument(
            priceId,
            price.getPrice(),
            price.getCurrency()
        );
        mongoPricerepository.save(document);
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("crypto.price.save.mongo.time: {}ms", processingTime);
        
        // Send metric to metrics server
        Timer timer = Timer.builder("crypto.price.save.mongo.time")
                .description("Time taken to save crypto price updates to mongo")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
    }

}
