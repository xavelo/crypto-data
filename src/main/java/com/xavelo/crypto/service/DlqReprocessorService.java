package com.xavelo.crypto.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DlqReprocessorService {

    private static final Logger logger = LoggerFactory.getLogger(DlqReprocessorService.class);

    private KafkaConsumer<String, String> dlqConsumer;    
    private CryptoPriceUpdatesService cryptoPriceUpdatesService;

    public DlqReprocessorService(CryptoPriceUpdatesService cryptoPriceUpdatesService, KafkaConsumer<String, String> dlqConsumer) {        
        this.cryptoPriceUpdatesService = cryptoPriceUpdatesService;
        this.dlqConsumer = dlqConsumer;
    }

    /*
    @Scheduled(fixedDelay = 300000) // running every 5 minutes
    public List<String> scheduledReprocessDlqMessages() {
        return reprocessDlqMessages(10);
    }
    */
    
    public List<String> reprocessDlqMessages(int numberOfRecords) {
        logger.info("----------> dlq reprocessDlqMessages({})", numberOfRecords);
        List<String> reprocessedRecords = new ArrayList<String>();
  
        dlqConsumer.poll(Duration.ofMillis(0));
        Set<TopicPartition> partitions = dlqConsumer.assignment();
        if (partitions.isEmpty()) {
            logger.warn("dlq - No partitions assigned to the consumer");
            return reprocessedRecords;
        }        
        dlqConsumer.resume(partitions);

        // Get the current offsets for each partition
        Map<Integer,Long> partitionOffsets = new HashMap<>();
        long currentOffset = 0;
        for (TopicPartition partition : partitions) {
            currentOffset = dlqConsumer.position(partition);
            partitionOffsets.put(partition.partition(), currentOffset);  // Get current offset
            logger.info("dlq - Current offset for partition {} is {}", partition.partition(), currentOffset);
        }

        ConsumerRecords<String, String> records = dlqConsumer.poll(Duration.ofMillis(1000));
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
                dlqConsumer.commitSync(Collections.singletonMap(new TopicPartition(record.topic(), record.partition()), 
                                                      new OffsetAndMetadata(record.offset() + 1)));                                
                logger.info("dlq {} reprocessed records", recordsProcessed);
            }      
        }
        // Seek back to the current position after processing to avoid skipping unprocessed records
        partitions = dlqConsumer.assignment();
        logger.info("dlq partitions check: {}", partitions);
        for (TopicPartition partition : partitions) {
            long position = currentOffset + recordsProcessed;
            logger.info("dlq - Resetting position for partition {} to {}", partition.partition(), position);
            dlqConsumer.seek(partition, position);
        }
        dlqConsumer.pause(partitions);
        return reprocessedRecords;
    }

}
