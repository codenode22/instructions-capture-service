package com.example.instructions.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class KafkaListenerService {
    private final TradeTransformer tradeTransformer;

    private final KafkaPublisher KafkaPublisher;
    
    public KafkaListenerService(TradeTransformer tradeTransformer,KafkaPublisher KafkaPublisher) {
        this.tradeTransformer = tradeTransformer;
        this.KafkaPublisher = KafkaPublisher;
    }


    @KafkaListener(topics = "instructions.inbound", groupId = "${spring.kafka.consumer.group-id}")
    public void listenInboundInstructions(String message) throws JsonProcessingException {
        // Log the message and perform your business logic
        System.out.println("Received message from instructions.inbound - " + message);
        // 1. Deserializing the JSON string into your CanonicalTrade object.
        CanonicalTrade canonicalTrade = tradeTransformer.deserializationToCanonicalTrade(message);
        // 2. Transforming the data to a PlatformTrade object.
        PlatformTrade platformTrade = tradeTransformer.transform(canonicalTrade);
        // 3. Publishing the transformed data to the 'instructions.outbound' topic.
        KafkaPublisher.sendMessage("instructions.outbound", platformTrade.toJson());
    }

    @KafkaListener(topics = "instructions.outbound", groupId = "${spring.kafka.consumer.group-id}")
    public void listenOutboundInstructions(String message) {
        System.out.println("Received message from instructions.outbound - " + message);
    }
}

