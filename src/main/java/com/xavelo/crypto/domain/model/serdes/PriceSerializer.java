package com.xavelo.crypto.domain.model.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.domain.model.Price;

public class PriceSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializePrice(Price price) {
        try {
            return objectMapper.writeValueAsString(price);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing Price object", e);
        }
    }

    public static Price deserializePrice(String json) {
        try {
            return objectMapper.readValue(json, Price.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing Price object", e);
        }
    }
}
