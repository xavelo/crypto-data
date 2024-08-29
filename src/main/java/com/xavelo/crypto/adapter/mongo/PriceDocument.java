package com.xavelo.crypto.adapter.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "crypto-price-updates")
public class PriceDocument {

    @Id
    private CryptoPriceId id;
    private BigDecimal price;
    private String currency;
    private Instant timestamp;

    // Embedded class for the compound _id
    public static class CryptoPriceId {
        private String coinId;
        private Instant timestamp;

        public CryptoPriceId(String coinId, Instant timestamp) {
            this.coinId = coinId;
            this.timestamp = timestamp;
        }

        // Getters and setters
    }

    public PriceDocument(String coinId, Instant timestamp, BigDecimal price, String currency) {
        this.id = new CryptoPriceId(coinId, timestamp);
        this.price = price;
        this.currency = currency;
    }

    // Getters and setters
}
