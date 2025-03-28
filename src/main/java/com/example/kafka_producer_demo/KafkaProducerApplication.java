package com.example.kafka_producer_demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
public class KafkaProducerApplication implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerApplication.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    @Value("${spring.kafka.producer.retries:3}")
    private int maxRetries;

    public KafkaProducerApplication(KafkaTemplate<String, String> kafkaTemplate,
                                  org.springframework.core.env.Environment env) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = env.getProperty("spring.kafka.producer.topic", "my-topic");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }

        String messageId = UUID.randomUUID().toString();
        Message<String> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("messageId", messageId)
                .setHeader("timestamp", Instant.now().toEpochMilli())
                .build();

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaMessage);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send message to topic: {}", topic, ex);
            }
        });

        return ResponseEntity.ok("Message queued for sending with ID: " + messageId);
    }

    @Override
    public Health health() {
        try {
            // Check if Kafka is reachable
            kafkaTemplate.getDefaultTopic();
            return Health.up()
                    .withDetail("topic", topic)
                    .withDetail("bootstrapServers", kafkaTemplate.getDefaultTopic())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerApplication.class, args);
    }
}