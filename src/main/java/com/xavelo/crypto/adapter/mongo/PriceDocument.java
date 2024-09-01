package com.xavelo.crypto.adapter.mongo;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "crypto-price-updates")
@Getter
public class PriceDocument {

    @Id
    private PriceId id;
    private BigDecimal price;
    private String currency;
    private Instant timestamp;

    @Getter
    public static class PriceId {

        private String coin;
        private Instant timestamp;

        public PriceId(String coin, Instant timestamp) {
            this.coin = coin;
            this.timestamp = timestamp;
        }

    }

    public PriceDocument(PriceId id, BigDecimal price, String currency) {
        this.id = id;
        this.price = price;
        this.currency = currency;
    }

}
