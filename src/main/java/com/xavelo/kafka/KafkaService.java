package com.xavelo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @KafkaListener(topics = "test-topic", groupId = "test-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message) {
        logger.info("Received message: {}", message);
        String sql = "INSERT INTO messages (message) VALUES (?)";
        jdbcTemplate.update(sql, message);
    }

}

