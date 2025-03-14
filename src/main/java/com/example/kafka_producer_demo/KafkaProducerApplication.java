package com.example.kafka_producer_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KafkaProducerApplication {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaProducerApplication(KafkaTemplate<String, String> kafkaTemplate,
                                    org.springframework.core.env.Environment env) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = env.getProperty("spring.kafka.producer.topic", "my-topic");
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        kafkaTemplate.send(topic, message);
        return "Message sent to " + topic + ": " + message;
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerApplication.class, args);
    }
}