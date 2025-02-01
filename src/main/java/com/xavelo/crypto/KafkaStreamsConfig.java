package com.xavelo.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.model.serdes.BigDecimalSerde;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.math.BigDecimal;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KStream<String, BigDecimal> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream("crypto-price-updates-topic");
    
        KStream<String, BigDecimal> bigDecimalStream = stream
            .mapValues(value -> {
                try {                    
                    Price price = objectMapper.readValue(value, Price.class);
                    logger.info("*** logsstreams price value for {}: {}", price.getCoin(), price.getPrice());
                    return price.getPrice();
                } catch (Exception e) {
                    logger.error("Failed to deserialize Price from JSON: " + value, e);
                    return null;
                }
            })
            .filter((key, value) -> value != null);
    
        KTable<Windowed<String>, BigDecimal> averageBTCPrices = bigDecimalStream
            .groupBy((key, value) -> "BTC")
            .windowedBy(TimeWindows.of(Duration.ofHours(4)))
            .aggregate(
                () -> BigDecimal.ZERO,
                (key, value, aggregate) -> {
                    // Calculate the new average price
                    // You will need to maintain the count of prices in a more complex implementation
                    logger.info("*** streams calculate average price with key {} and value {}", key, value);
                    return aggregate.add(value); // Update the aggregate
                },
                Materialized.with(Serdes.String(), new BigDecimalSerde())
            );
    
        // You may want to write the average prices to a new topic or log them
        averageBTCPrices.toStream().foreach((windowedKey, averagePrice) -> {
            logger.info(String.format("Average BTC Price in the last 4 hours: %s, Average Price: %s", 
                windowedKey.key(), averagePrice));
        });
    
        return bigDecimalStream; // Return the transformed stream
    }

}