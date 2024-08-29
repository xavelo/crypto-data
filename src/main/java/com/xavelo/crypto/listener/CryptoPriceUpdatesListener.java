package com.xavelo.crypto.listener;

import com.xavelo.crypto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private static final String CRYPTO_PRICE_UPDATES_COLLECTION = "crypto-price-updates";
    @Autowired
    private MongoTemplate mongoTemplate;

    @KafkaListener(topics = "crypto-price-updates-topic", groupId = "crypto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        logger.info("Received message: key {} - value {}", key, message);
        checkCollection();
        saveMessage(key, message);
        Message msg = findMessageByKey("key");
        logger.info("Read message by key {}: {}", key, msg.getValue());
    }

    private void checkCollection() {
        if (!mongoTemplate.collectionExists(CRYPTO_PRICE_UPDATES_COLLECTION)) {
            logger.info("Creating collection {}", CRYPTO_PRICE_UPDATES_COLLECTION);
            mongoTemplate.createCollection(CRYPTO_PRICE_UPDATES_COLLECTION);
        }
    }

    private void saveMessage(String key, String message) {
        Message msg = new Message(key, message);
        mongoTemplate.save(msg, CRYPTO_PRICE_UPDATES_COLLECTION);
        logger.info("Message with key {} saved",  msg.getKey());
    }

    private Message findMessageByKey(String key) {
        Message msg = mongoTemplate.findOne(Query.query(Criteria.where("key").is(key)), Message.class, CRYPTO_PRICE_UPDATES_COLLECTION);
        return msg;
    }

}

