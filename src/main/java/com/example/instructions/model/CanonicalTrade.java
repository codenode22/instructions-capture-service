package com.example.instructions.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Data
public class CanonicalTrade {
    @JsonProperty("platform_id")
    private String platformId;
    
    private String account;
    
    private String security;
    
    private String type;
    
    private BigDecimal amount;
    
    private String timestamp;

    //Normalizes and validates the trade fields according to business rules.
    public void normalize() {
        this.type = normalizeTradeType(this.type);
        this.security = normalizeSecurityId(this.security);
        this.account = maskAccountNumber(this.account);
    }

    // --- Private Helper Methods for Normalization Logic ---

    /**
     * Normalizes the trade type string to a standard single-character code.
     * @param rawType The raw trade type string.
     * @return The normalized code (e.g., 'B', 'S', or original if not recognized).
     */
    private String normalizeTradeType(String rawType) {
        if (rawType == null) return null;
        
        String upperType = rawType.trim().toUpperCase();
        
        if (upperType.equals("BUY") || upperType.startsWith("B")) {
            return "B";
        } else if (upperType.equals("SELL") || upperType.startsWith("S")) {
            return "S";
        }
        // Default: return the original string if no match is found, or throw an exception
        return rawType; 
    }

    /**
     * Converts security ID to uppercase. Includes a simple validation check.
     * @param rawSecurityId The raw security ID string.
     * @return The uppercase security ID.
     */
    private String normalizeSecurityId(String rawSecurityId) {
        if (rawSecurityId == null) return null;
        
        String upperSecurity = rawSecurityId.trim().toUpperCase();
        
        // Basic Validation: Ensure it contains only alphanumeric characters (e.g., for ticker symbols)
        if (!upperSecurity.matches("[A-Z0-9]+")) {
            // Log a warning or throw a validation exception in a real application
            System.err.println("Warning: Invalid characters found in security ID: " + rawSecurityId);
        }
        
        return upperSecurity;
    }

    /**
     * Masks all but the last four characters of the account number.
     * @param rawAccountNumber The raw account number string.
     * @return The masked account number (e.g., "*******1234").
     */
    private String maskAccountNumber(String rawAccountNumber) {
        if (rawAccountNumber == null || rawAccountNumber.length() < 4) {
            return rawAccountNumber; // Cannot mask or too short
        }

        int length = rawAccountNumber.length();
        String lastFour = rawAccountNumber.substring(length - 4);
        
        // Create a string of asterisks for the rest of the length
        String maskedPrefix = "*".repeat(length - 4);
        
        return maskedPrefix + lastFour;
    }


    //Converts the PlatformTrade object to its JSON string representation.
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
