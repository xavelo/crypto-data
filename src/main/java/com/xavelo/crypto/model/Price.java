package com.xavelo.crypto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    @JsonProperty("coin")
    private String coin;
    @JsonProperty("price")
    private BigDecimal price;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("timestamp")
    private Instant timestamp;
}
