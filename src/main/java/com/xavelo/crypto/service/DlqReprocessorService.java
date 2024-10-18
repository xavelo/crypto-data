package com.xavelo.crypto.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DlqReprocessorService {

    private static final Logger logger = LoggerFactory.getLogger(DlqReprocessorService.class);

    private KafkaConsumer<String, String> consumer;    
    private CryptoPriceUpdatesService cryptoPriceUpdatesService;

    private static final String DLQ_TOPIC = "crypto-price-updates-topic-dlq";

    public DlqReprocessorService(CryptoPriceUpdatesService cryptoPriceUpdatesService) {        
        this.cryptoPriceUpdatesService = cryptoPriceUpdatesService;
    }

    //@Scheduled(fixedDelay = 300000) // running every 5 minutes
    //public List<String> scheduledReprocessDlqMessages() {
    //    return reprocessDlqMessages(10);
    //}
    
    public List<String> reprocessDlqMessages(int numberOfRecords) {
        logger.info("----------> dlq reprocessDlqMessages({})", numberOfRecords);
        List<String> reprocessedRecords = new ArrayList<String>();
  
        consumer.poll(Duration.ofMillis(0));
        Set<TopicPartition> partitions = consumer.assignment();
        if (partitions.isEmpty()) {
            logger.warn("dlq - No partitions assigned to the consumer");
            return reprocessedRecords;
        }        
        consumer.resume(partitions);

        // Get the current offsets for each partition
        Map<Integer,Long> partitionOffsets = new HashMap<>();
        long currentOffset = 0;
        for (TopicPartition partition : partitions) {
            currentOffset = consumer.position(partition);
            partitionOffsets.put(partition.partition(), currentOffset);  // Get current offset
            logger.info("dlq - Current offset for partition {} is {}", partition.partition(), currentOffset);
        }

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        logger.info("dlq {} records polled", records.count());       
        int recordsToProcess = Math.min(records.count(), numberOfRecords);
        int recordsProcessed = 0;
        for (var record : records) {
            if (recordsProcessed < recordsToProcess) {
                logger.info("dlq -");
                logger.info("dlq reprocessing record: key={} value={}", record.key(), record.value());                
                cryptoPriceUpdatesService.process(record);
                reprocessedRecords.add(record.value());
                recordsProcessed++;
                // Commit the offset for the specific record
                logger.info("dlq Committing offset {} for partition {}", record.offset() + 1, record.partition());
                consumer.commitSync(Collections.singletonMap(new TopicPartition(record.topic(), record.partition()), 
                                                      new OffsetAndMetadata(record.offset() + 1)));                                
                logger.info("dlq {} reprocessed records", recordsProcessed);
            }      
        }
        // Seek back to the current position after processing to avoid skipping unprocessed records
        partitions = consumer.assignment();
        logger.info("dlq partitions check: {}", partitions);
        for (TopicPartition partition : partitions) {
            long position = currentOffset + recordsProcessed;
            logger.info("dlq - Resetting position for partition {} to {}", partition.partition(), position);
            consumer.seek(partition, position);
        }
        consumer.pause(partitions);
        return reprocessedRecords;
    }

    @PostConstruct
    public void initDlqConsumer() {
        logger.info("dlq reprocess initDlqConsumer");
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap.default.svc:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-reprocessor-group");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "dlq-reprocessor-client");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000"); // 30 seconds
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5 minutes
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000"); // 3 seconds

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(DLQ_TOPIC));
        consumer.poll(Duration.ofMillis(0)); // Ensure partitions are assigned
        Set<TopicPartition> partitions = consumer.assignment(); // Get assigned partitions
        logger.info("dlq reprocess assigned partitions: {}", partitions.size());
        consumer.pause(partitions);         
    }


}
