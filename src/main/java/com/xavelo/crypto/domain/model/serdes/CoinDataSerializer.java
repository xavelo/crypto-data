package com.xavelo.crypto.domain.model.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xavelo.crypto.domain.model.CoinData;

public class CoinDataSerializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializeCoinData(CoinData coinData) {
        try {
            return objectMapper.writeValueAsString(coinData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing CoinData object", e);
        }
    }

    public static CoinData deserializeCoinData(String json) {
        try {
            return objectMapper.readValue(json, CoinData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing CoinData object", e);
        }
    }

}
