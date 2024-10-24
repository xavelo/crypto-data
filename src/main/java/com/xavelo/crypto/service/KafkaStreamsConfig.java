package com.xavelo.crypto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.model.Price;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream("crypto-price-updates-topic");

        stream.mapValues(value -> {
            try {
                // Deserialize the JSON string to a Price object
                Price price = objectMapper.readValue(value, Price.class);
                // Log the price
                logger.info(String.format("streams - Received price update: Coin: %s, Price: %s, Currency: %s, Timestamp: %s",
                        price.getCoin(), price.getPrice(), price.getCurrency(), price.getTimestamp()));
                return price; // Return the Price object if needed
            } catch (Exception e) {
                logger.error("Failed to deserialize Price from JSON: " + value, e);
                return null; // Handle invalid JSON
            }
        }).filter((key, value) -> key == "BTC"); // Filter out invalid Price objects

        return stream; // Return the KStream
    }
}