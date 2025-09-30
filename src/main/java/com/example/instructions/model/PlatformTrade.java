package com.example.instructions.model;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Data
public class PlatformTrade {
    @JsonProperty("platform_id")
    private String platformId;
    
    private Trade trade;

    @Data
    public static class Trade {
        private String account;
        private String security;
        private String type;
        private BigDecimal amount;
        private String timestamp;
    }

    /**
     * Converts the PlatformTrade object to its JSON string representation.
     *
     * @return A JSON string representing the PlatformTrade object.
     * @throws JsonProcessingException if an error occurs during serialization.
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        
        return objectMapper.writeValueAsString(this);
    }
}
