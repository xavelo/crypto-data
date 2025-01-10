package com.xavelo.crypto.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.adapter.influxdb.InfluxDBAdapter;
import com.xavelo.crypto.model.Price;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class CryptoPriceUpdatesService {
    
    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesService.class);

    private PriceService priceService;
    private InfluxDBAdapter influxDBAdapter;
    private ObjectMapper objectMapper;
    private MeterRegistry meterRegistry;
    private KafkaTemplate<String, String> kafkaTemplate;
    private Counter retryCounter;

    private static final int MAX_RETRIES = 3; 

    public CryptoPriceUpdatesService(PriceService priceService, 
                                    InfluxDBAdapter influxDBAdapter, 
                                    ObjectMapper objectMapper, 
                                    MeterRegistry meterRegistry, 
                                    KafkaTemplate kafkaTemplate) {
        this.priceService = priceService;
        this.influxDBAdapter = influxDBAdapter;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
        this.retryCounter = Counter.builder("crypto.price.processing.retries") // Initialize the counter
        .description("Count of retries for processing crypto price updates")
        .register(meterRegistry);
    }

    public void process(ConsumerRecord<String, String> record) {

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                long startTime = System.nanoTime();

                Price price = objectMapper.readValue(record.value(), Price.class);
                // simulate errors to test retry mechanism and observability
                //simulateUnreliableApiCall(0);
                priceService.savePriceUpdate(price);
                //influxDBAdapter.writePriceUpdate(price);

                long processingTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
                logger.info("crypto.price.processing.time: {}ms", processingTime);

                Timer timer = Timer.builder("crypto.price.processing.time")
                        .description("Time taken to process crypto price updates")
                        .register(meterRegistry);
                timer.record(processingTime, TimeUnit.MILLISECONDS);
                
                return;

            } catch (Exception e) {
                attempt++;
                retryCounter.increment(); // Increment the retry counter
                logger.error("Attempt {}: error {} processing record {}", attempt, e.getMessage(), record);
                if (attempt >= MAX_RETRIES) {
                    sendToDLQ(record); // Send to DLQ after max retries
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

    private void sendToDLQ(ConsumerRecord<String, String> originalRecord) {
        ConsumerRecord<String, String> dlqRecord = new ConsumerRecord<>(
            "crypto-price-updates-topic-dlq",
            originalRecord.partition(),
            originalRecord.offset(),
            originalRecord.key(),
            originalRecord.value()
        );
        kafkaTemplate.send(dlqRecord.topic(), dlqRecord.key(), dlqRecord.value());
    }

    private void simulateUnreliableApiCall(int errorPercentage) {
        // Generate a random number between 0 and 100
        int randomValue = (int) (Math.random() * 100);
        if (randomValue < errorPercentage) {
            throw new RuntimeException("Simulated API call failure");
        }
    }

}
