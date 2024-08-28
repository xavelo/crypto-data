package com.xavelo.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class CryptoPriceUpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(CryptoPriceUpdatesListener.class);

    private static final String CRYPTO_PRICE_UPDATES_COLLECTION = "crypto-price-updates";
    @Autowired
    private MongoTemplate mongoTemplate;

    @org.springframework.kafka.annotation.KafkaListener(topics = "crypto-price-updates-topic", groupId = "cryppto-price-updates-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message) {
        logger.info("Received message: {}", message);
        checkCollection();
        //saveMessage(message);
        //Message msg = findMessageByKey("key");
        //logger.info("Read message: {}", msg.getValue());
    }

    private void checkCollection() {
        if (!mongoTemplate.collectionExists(CRYPTO_PRICE_UPDATES_COLLECTION)) {
            logger.info("Creating collection {}", CRYPTO_PRICE_UPDATES_COLLECTION);
            mongoTemplate.createCollection(CRYPTO_PRICE_UPDATES_COLLECTION);
        } else {
            logger.info("Collection {} already existing", CRYPTO_PRICE_UPDATES_COLLECTION);
        }
    }

    private void saveMessage(String message) {
        Message msg = new Message("key", message);
        mongoTemplate.save(msg, CRYPTO_PRICE_UPDATES_COLLECTION);
        logger.info("Message with key {} saved",  msg.getKey());
    }

    private Message findMessageByKey(String key) {
        Message msg = mongoTemplate.findOne(Query.query(Criteria.where("key").is(key)), Message.class, CRYPTO_PRICE_UPDATES_COLLECTION);
        return msg;
    }

}

