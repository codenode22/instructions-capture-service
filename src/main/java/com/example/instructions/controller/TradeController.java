package com.example.instructions.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.service.KafkaPublisher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean; // Used for thread-safe flag
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;



@RestController
@Tag(name = "Trade Processing", description = "Endpoints for handling inbound trade instructions.") // 1. Tag for grouping
public class TradeController {

    private final KafkaPublisher kafkaPublisher;
    private final ConcurrentMap<String, CanonicalTrade> tradeStoreConcurrentMap = new ConcurrentHashMap<>();
    
    public TradeController(KafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    @PostMapping("/trades")
    @Operation(
        summary = "Process a list of raw trade objects",
        description = "Receives trade data for Kafka processing.",
        
        // --- FIX IS HERE: Use the fully qualified name for the Swagger annotation ---
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( 
            description = "A list of raw trade objects to be processed.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class),
                examples = @ExampleObject(
                    name = "ExampleTradeList",
                    value = "[{\"platform_id\": \"P001\", \"account\": \"A123\", \"security\": \"AAPL\", \"type\": \"BUY\", \"amount\": \"100.50\", \"timestamp\": \"2025-09-30T10:00:00Z\"}]"
                )
            )
        )
    )
    
    @ApiResponse(responseCode = "200", description = "Successfully processed all trades.") // 4. Define successful response
    @ApiResponse(responseCode = "500", description = "Processing failed. Trade kept in store for retry.") // 5. Define error response

    public ResponseEntity<String> processTrades(@RequestBody List<Map<String, String>> tradeObjects) {
        
        System.out.println("Received " + tradeObjects.size() + " trade objects.");

        // Use AtomicBoolean for a thread-safe flag to track if any trade failed
        AtomicBoolean kafkaSendFailed = new AtomicBoolean(false);

        // Convert the list to a parallel stream for concurrent processing
        tradeObjects.parallelStream()
            
            // 1. Map: Convert the raw Map data to the CanonicalTrade DTO
            .map(this::mapToCanonicalTrade)
            
            // 2. Peek: Apply normalization (in-place modification)
            .peek(CanonicalTrade::normalize)
            
            // 3. ForEach: Perform the I/O-bound tasks concurrently
            .forEach(canonicalTrade -> {
                
                String uniqueKey = canonicalTrade.getPlatformId() + ":" + canonicalTrade.getTimestamp();
                
                // Store for auditing/retry (must be thread-safe)
                tradeStoreConcurrentMap.put(uniqueKey, canonicalTrade);
                try {
                    System.out.println(canonicalTrade.toJson());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                try {
                    // Send to Kafka (must be thread-safe)
                    this.sendToTopic("instructions.inbound", canonicalTrade.toJson());
                    
                    // If successful, remove from the retry store
                    tradeStoreConcurrentMap.remove(uniqueKey);

                } catch (Exception e) {
                    // Set the failure flag, but allow other trades to continue processing
                    kafkaSendFailed.set(true); 
                    System.err.println("Kafka publish failed for trade " + uniqueKey + ". Trade left in store for retry.");
                    // The trade remains in the store for retry as per original logic
                }
            });

        // Check the failure flag and return the appropriate ResponseEntity
        if (kafkaSendFailed.get()) {
            return ResponseEntity.status(500)
                .body("Processing completed with some Kafka publishing failures. Failed trades left in store for retry.");
        } else {
            return ResponseEntity.ok("Successfully processed " + tradeObjects.size() + " trades.");
        }
    }
   
    /**
     * Helper method to map raw Map data to the CanonicalTrade DTO.
     * @param trade The raw map representing the trade.
     * @return A populated CanonicalTrade object.
     */
    private CanonicalTrade mapToCanonicalTrade(Map<String, String> trade) {
        CanonicalTrade canonicalTrade = new CanonicalTrade();
        
        try {
            // Added explicit error handling for robustness
            BigDecimal amountAsBigD = new BigDecimal(trade.get("amount")); 
            canonicalTrade.setAmount(amountAsBigD);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing amount for trade: " + trade + ". Defaulting to zero.");
            canonicalTrade.setAmount(BigDecimal.ZERO);
        }

        canonicalTrade.setPlatformId(trade.get("platform_id"));
        canonicalTrade.setAccount(trade.get("account"));
        canonicalTrade.setSecurity(trade.get("security"));
        canonicalTrade.setType(trade.get("type"));
        canonicalTrade.setTimestamp(trade.get("timestamp"));
        return canonicalTrade;
    }


    public void sendToTopic(String topicName, String request) {
        kafkaPublisher.sendMessage(topicName, request);
    }
   
}
