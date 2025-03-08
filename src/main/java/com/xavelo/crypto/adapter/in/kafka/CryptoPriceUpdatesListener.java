package com.xavelo.crypto.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.application.port.out.StorePriceUpdatePort;

import com.xavelo.crypto.domain.model.Price;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private final Counter priceUpdateCounter = Metrics.counter("crypto.data.price.updates");

    private StorePriceUpdatePort storePriceUpdatePort;

    @Timed(value = "crypto.data.price.update.time", description = "Time to process crypto price update")
    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException, InterruptedException {
        priceUpdateCounter.increment();
        logger.debug("Received message: key {} - value {}", record.key(), record.value());
        Price price = new ObjectMapper().readValue(record.value(), Price.class);
        storePriceUpdatePort.storePriceUpdate(price);
        acknowledgment.acknowledge();
    }

}
