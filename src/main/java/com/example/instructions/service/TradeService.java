package com.example.instructions.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;
import com.example.instructions.model.CanonicalTrade;

@Service
public class TradeService {
    private final ConcurrentMap<String, CanonicalTrade> tradeStore = new ConcurrentHashMap<>();

    public void storeTrade(CanonicalTrade trade,String key) {
        tradeStore.put(key, trade);
        System.out.println("Stored trade with key: " + key + ". Current size: " + tradeStore.size());
    }

    public CanonicalTrade retrieveTrade(String key) {
        return tradeStore.get(key);
    }

    public boolean removeTrade(String key) {
        CanonicalTrade removedTrade = tradeStore.remove(key);
        return removedTrade != null;
    }

    public int getSize() {
        return tradeStore.size();
    }
}