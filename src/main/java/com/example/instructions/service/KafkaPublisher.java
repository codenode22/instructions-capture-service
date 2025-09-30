package com.example.instructions.service;

import org.springframework.kafka.core.KafkaTemplate;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topicName, String message) {
        System.out.println("Producing message to " + topicName + ": " + message);
              
        // 1. Send the message. This method now returns a CompletableFuture.
        CompletableFuture<SendResult<String, String>> future = 
            kafkaTemplate.send(topicName, message);

        // 2. Attach modern asynchronous callbacks (non-blocking)
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // Success Callback: Executed when the message is acknowledged by Kafka
                System.out.printf(
                    "Successfully sent message to topic %s on partition %d with offset %d%n",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                // Failure Callback: Executed if the send operation fails after retries
                System.err.printf("Failed to send message to topic %s. Error: %s%n", 
                                  topicName, ex.getMessage());
            }
        });
    }
}
