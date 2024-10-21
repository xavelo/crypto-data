package com.xavelo.crypto;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableScheduling
public class CryptoDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoDataApplication.class, args);
	}

	@Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://192.168.1.94:30012");
        config.addAllowedMethod("GET");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public KafkaConsumer<String, String> dlqConsumer() {
        String DLQ_TOPIC = "crypto-price-updates-topic-dlq";
        System.out.println("dlq reprocess initDlqConsumer");
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "my-cluster-kafka-bootstrap.default.svc:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-reprocessor-group" + System.currentTimeMillis());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "dlq-reprocessor-client" + System.currentTimeMillis());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");        
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "300000"); // 5 minutes
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"); // 5 minutes
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000"); // 3 seconds

        KafkaConsumer<String, String> dlqConsumer = new KafkaConsumer<>(props);
        dlqConsumer.subscribe(Collections.singletonList(DLQ_TOPIC));
        dlqConsumer.poll(Duration.ofMillis(0)); // Ensure partitions are assigned
        Set<TopicPartition> partitions = dlqConsumer.assignment(); // Get assigned partitions
        System.out.println("dlq reprocess assigned partitions: {"+partitions.size()+"}");
        dlqConsumer.pause(partitions);     
        return dlqConsumer;    
    }

}
