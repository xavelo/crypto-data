package com.xavelo.crypto.infrastructure.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class CryptoDataListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoDataListener.class);

    @KafkaListener(topics = "crypto-data-topic", groupId = "crypto-data-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException, InterruptedException {
        logger.debug("Received message: key {} - value {}", record.key(), record.value());
        acknowledgment.acknowledge();
    }

}
