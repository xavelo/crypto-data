package com.xavelo.crypto.api;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

    private static final Logger logger = LoggerFactory.getLogger(DlqController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DLQ_TOPIC = "crypto-price-updates-topic-dlq";

    public DlqController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @PostMapping("/process")
    public ResponseEntity<String> processRecords(@RequestParam int numberOfRecords) {
        consumeRecordsFromTopic(DLQ_TOPIC, numberOfRecords);
        return ResponseEntity.ok("Processing completed"); // Return a response
    }
    
    // New method to consume records from the specified topic
    private void consumeRecordsFromTopic(String topic, int numberOfRecords) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap.default.svc:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-reprocessor-group");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "dlq-reprocessor-client");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(DLQ_TOPIC));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            int recordsToProcess = Math.min(records.count(), numberOfRecords);
            int recordsProcessed = 0;
            for (var record : records) {
                if (recordsProcessed <= recordsToProcess) {
                    logger.info("Reprocessing record: key={} value={}", record.key(), record.value());                
                    //kafkaTemplate.send("crypto-price-updates-topic", record.key(), record.value());
                    recordsProcessed++;
            }
        }
    }
}

}

