package com.xavelo.crypto.infrastructure.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xavelo.crypto.application.price.PriceUpdatesService;

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

    private PriceUpdatesService priceUpdatesService;

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException, InterruptedException {
        logger.debug("Received message: key {} - value {}", record.key(), record.value());
        priceUpdatesService.process(record);
        acknowledgment.acknowledge();
    }

}
