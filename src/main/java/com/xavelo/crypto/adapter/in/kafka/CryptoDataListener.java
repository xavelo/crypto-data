package com.xavelo.crypto.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.application.port.StoreCoinDataUseCase;
import com.xavelo.crypto.domain.model.CoinData;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CryptoDataListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoDataListener.class);

    private StoreCoinDataUseCase storeCoinDataUseCase;

    @KafkaListener(topics = "crypto-data-topic", groupId = "crypto-data-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException, InterruptedException {
        logger.debug("Received message: key {} - value {}", record.key(), record.value());
        CoinData coindata = new ObjectMapper().readValue(record.value(), CoinData.class);
        storeCoinDataUseCase.storeCoinData(coindata);
        acknowledgment.acknowledge();
    }

}
