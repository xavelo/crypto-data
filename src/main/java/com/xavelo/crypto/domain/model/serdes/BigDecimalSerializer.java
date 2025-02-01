package com.xavelo.crypto.domain.model.serdes;

import org.apache.kafka.common.serialization.Serializer;

import java.math.BigDecimal;
import java.util.Map;

public class BigDecimalSerializer implements Serializer<BigDecimal> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, BigDecimal data) {
        if (data == null) {
            return null;
        }
        return data.toString().getBytes(); // Convert BigDecimal to byte array
    }

    @Override
    public void close() {
    }
}

