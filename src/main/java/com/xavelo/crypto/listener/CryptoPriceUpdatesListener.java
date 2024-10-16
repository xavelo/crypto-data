package com.xavelo.crypto.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.adapter.influxdb.InfluxDBAdapter;
import com.xavelo.crypto.service.PriceService;
import com.xavelo.crypto.model.Price;

import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

@Service
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private final PriceService priceService;
    private final InfluxDBAdapter influxDBAdapter;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Counter retryCounter; // Add a counter for retries

    private static final int MAX_RETRIES = 3; 
    
    public CryptoPriceUpdatesListener(PriceService priceService,                                       
                                      InfluxDBAdapter influxDBAdapter,                                   
                                      ObjectMapper objectMapper, 
                                      MeterRegistry meterRegistry,
                                      KafkaTemplate<String, String> kafkaTemplate) {
        this.priceService = priceService;        
        this.influxDBAdapter = influxDBAdapter;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
        this.retryCounter = Counter.builder("crypto.price.processing.retries") // Initialize the counter
                .description("Count of retries for processing crypto price updates")
                .register(meterRegistry);
    }

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException, InterruptedException {
        logger.info("Received message: key {} - value {}", record.key(), record.value());        
        process(record);        
        acknowledgment.acknowledge();
    }

    private void process(ConsumerRecord<String, String> record) {

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                long startTime = System.nanoTime();

                Price price = objectMapper.readValue(record.value(), Price.class);
                // simulate errors to test retry mechanism and observability
                simulateUnreliableApiCall(95);
                priceService.savePriceUpdate(price);
                influxDBAdapter.writePriceUpdate(price);

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
