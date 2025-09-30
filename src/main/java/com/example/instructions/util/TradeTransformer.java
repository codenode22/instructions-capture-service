package com.example.instructions.util;

import org.springframework.stereotype.Component;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class TradeTransformer {
    /**
     * Transforms a CanonicalTrade object into a PlatformTrade object,
     * copying the fields directly without modification.
     *
     * @param canonicalTrade The input object with raw trade data.
     * @return A new PlatformTrade object with the data copied directly.
     */
    public PlatformTrade transform(CanonicalTrade canonicalTrade) {
        // Create the output DTOs
        PlatformTrade platformTrade = new PlatformTrade();
        PlatformTrade.Trade transformedTrade = new PlatformTrade.Trade();

        // Copy fields directly without transformation
        transformedTrade.setAccount(canonicalTrade.getAccount());
        transformedTrade.setSecurity(canonicalTrade.getSecurity());
        transformedTrade.setType(canonicalTrade.getType());
        transformedTrade.setAmount(canonicalTrade.getAmount());
        transformedTrade.setTimestamp(canonicalTrade.getTimestamp());
        
        platformTrade.setPlatformId(canonicalTrade.getPlatformId());
        platformTrade.setTrade(transformedTrade);

        return platformTrade;
    }

    public CanonicalTrade deserializationToCanonicalTrade(String message){
        ObjectMapper objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle Java 8 Date and Time API types, such as Instant.
        objectMapper.registerModule(new JavaTimeModule());

        try {
            //Use the readValue() method to convert the JSON string to a CanonicalTrade object
            CanonicalTrade trade = objectMapper.readValue(message, CanonicalTrade.class);
            return trade;
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
        
        
    }
}
