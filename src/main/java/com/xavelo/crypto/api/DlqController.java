package com.xavelo.crypto.api;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

    private static final Logger logger = LoggerFactory.getLogger(DlqController.class);

    private KafkaConsumer<String, String> consumer;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DLQ_TOPIC = "crypto-price-updates-topic-dlq";

    public DlqController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @PostMapping("/process")
    public ResponseEntity<List<String>> processRecords(@RequestParam int numberOfRecords) {
        logger.info("-> reprocess {} DLQ records", numberOfRecords);
        List<String> records = consumeRecordsFromTopic(DLQ_TOPIC, numberOfRecords);
        return ResponseEntity.ok(records); // Return a response
    }
    
    // New method to consume records from the specified topic
    private List<String> consumeRecordsFromTopic(String topic, int numberOfRecords) {
        List<String> consumedRecords = new ArrayList<String>();
  
        consumer.poll(Duration.ofMillis(0));
        Set<TopicPartition> partitions = consumer.assignment();
        consumer.resume(partitions);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        logger.info("{} records consumed from crypto-price-updates-topic-dlq", records.count());            
        int recordsToProcess = Math.min(records.count(), numberOfRecords);
        int recordsProcessed = 0;
        for (var record : records) {
            if (recordsProcessed <= recordsToProcess) {
                logger.info("reprocessing record: key={} value={}", record.key(), record.value());                
                //kafkaTemplate.send("crypto-price-updates-topic", record.key(), record.value());
                consumedRecords.add(record.value());
                recordsProcessed++;
                consumer.commitSync();
            }      
        }
        consumer.pause(partitions);
        return consumedRecords;
    }

    @PostConstruct
    public void initConsumer() {
        logger.info("reprocess initConsumer");
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap.default.svc:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-reprocessor-group");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "dlq-reprocessor-client");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000"); // 30 seconds
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5 minutes
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000"); // 3 seconds

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(DLQ_TOPIC));
        consumer.poll(Duration.ofMillis(0)); // Ensure partitions are assigned
        Set<TopicPartition> partitions = consumer.assignment(); // Get assigned partitions
        logger.info("assigned partitions: {}", partitions.size());
        consumer.pause(partitions);         
    }

}


