package com.example.instructions.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.service.KafkaPublisher;
import com.example.instructions.service.TradeService;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

@RestController
public class TradeController {

    private final KafkaPublisher kafkaPublisher;
    private final TradeService tradeService;

    public TradeController(KafkaPublisher kafkaPublisher,TradeService tradeService) {
        this.kafkaPublisher = kafkaPublisher;
        this.tradeService = tradeService;
       
    }

    @PostMapping("/trades")
    public ResponseEntity<String> processTrades(@RequestBody List<Map<String, String>> tradeObjects ) throws JsonProcessingException {
        System.out.println("Received " + tradeObjects.size() + " trade objects.");
         //all this is done because double quotes are removed from messages after url encoding
        for (Map<String, String> trade : tradeObjects) {
          
            /* create the object inside the loop
            The Java Garbage Collector (GC) will then be able to reclaim the memory */
            CanonicalTrade canonicalTrade = new CanonicalTrade();
            BigDecimal amountAsBigD = new BigDecimal(trade.get("amount"));
            canonicalTrade.setPlatformId(trade.get("platform_id"));
            canonicalTrade.setAccount(trade.get("account"));
            canonicalTrade.setSecurity(trade.get("security"));
            canonicalTrade.setType(trade.get("type"));
            canonicalTrade.setAmount(amountAsBigD);
            canonicalTrade.setTimestamp(trade.get("timestamp"));

            canonicalTrade.normalize();

           // for simplicity let's assume 'platformId' + 'timestamp' is unique key.
            String uniqueKey = canonicalTrade.getPlatformId() + ":" + canonicalTrade.getTimestamp();
            tradeService.storeTrade(canonicalTrade,uniqueKey);
            System.out.println(canonicalTrade.toJson());
            try {
                this.sendToTopic("instructions.inbound",canonicalTrade.toJson());
                tradeService.removeTrade(uniqueKey);

            } catch (Exception e) {
                // 3. If processing fails (e.g., Kafka is down), the record remains in the map 
                //    for a separate retry mechanism to pick up later.
                return ResponseEntity.status(500).body("Processing failed. Trade kept in store for retry.");
            }
        }
        return ResponseEntity.ok("Successfully processed " + tradeObjects.size() + " trades.");

    }
   
    public void sendToTopic(String topicName,String request) {
        kafkaPublisher.sendMessage(topicName, request);
    }

}
