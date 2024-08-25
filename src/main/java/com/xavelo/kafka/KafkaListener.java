package com.xavelo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class KafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListener.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @org.springframework.kafka.annotation.KafkaListener(topics = "test-topic", groupId = "test-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message) {
        logger.info("Received message: {}", message);
        String sql = "INSERT INTO messages (message) VALUES (?)";
        checkCollection();
        saveMessage(message);
        Message msg = findMessageByKey("key");
        logger.info("Read message: {}", msg.getValue());
    }

    private void checkCollection() {
        if (!mongoTemplate.collectionExists("test-topic")) {
            logger.info("Creating collection test-topic");
            mongoTemplate.createCollection("test-topic");
        } else {
            logger.info("Collection test-topic already existing");
        }
    }

    private void saveMessage(String message) {
        Message msg = new Message("key", message);
        mongoTemplate.save(msg, "test_topic");
        logger.info("Message with key {} saved",  msg.getKey());
    }

    private Message findMessageByKey(String key) {
        Message msg = mongoTemplate.findOne(Query.query(Criteria.where("key").is(key)), Message.class, "test_topic");
        return msg;
    }

}

