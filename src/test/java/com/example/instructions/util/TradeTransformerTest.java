package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class TradeTransformerTest {

    @Test
    void testMainMethodLogic() throws IOException {
        String jsonString = """
            {
              "platform_id": "ACCT124",
              "account": "88815678",
              "security": "xyz789",
              "type": "Sell",
              "amount": 50000,
              "timestamp": "2025-08-05T10:20:15Z"
            }
            """;
        
        TradeTransformer tt = new TradeTransformer();
        
        CanonicalTrade ct = tt.deserializationToCanonicalTrade(jsonString);
        assertNotNull(ct, "Deserialized CanonicalTrade should not be null");

        PlatformTrade pt = tt.transform(ct);
        assertNotNull(pt, "Transformed PlatformTrade should not be null");

        System.out.println("result = "+ pt.toJson());

        //Add more assertions here to verify the content of pt
        assertEquals("ACCT124", pt.getPlatformId());
        assertEquals("88815678", pt.getTrade().getAccount());
    }
}
