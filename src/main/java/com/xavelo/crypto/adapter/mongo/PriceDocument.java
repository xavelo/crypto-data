package com.xavelo.crypto.adapter.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "crypto-price-updates")
public class PriceDocument {

    @Id
    private PriceId id;
    private BigDecimal price;
    private String currency;
    private Instant timestamp;

    // Embedded class for the compound _id
    public static class PriceId {

        private String coin;
        private Instant timestamp;

        public PriceId(String coin, Instant timestamp) {
            this.coin = coin;
            this.timestamp = timestamp;
        }

        // Getters and setters
    }

    public PriceDocument(String coin, Instant timestamp, BigDecimal price, String currency) {
        this.id = new PriceId(coin, timestamp);
        this.price = price;
        this.currency = currency;
    }

    // Getters and setters
}
