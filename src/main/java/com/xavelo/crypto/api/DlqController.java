package com.xavelo.crypto.api;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

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
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "lmy-cluster-kafka-bootstrap.default.svc:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-reprocessor-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(DLQ_TOPIC));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(record -> {
                // Reprocess each record
                // You can send it back to the original topic or process it directly
                System.out.printf("Reprocessing message: key=%s value=%s%n", record.key(), record.value());
                // Send back to the original topic
                kafkaTemplate.send("crypto-price-updates-topic", record.key(), record.value());
            });
        }
    }
}
