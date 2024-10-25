package com.xavelo.crypto.model.serdes;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

public class BigDecimalDeserializer implements Deserializer<BigDecimal> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public BigDecimal deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return new BigDecimal(new String(data)); // Convert byte array back to BigDecimal
    }

    @Override
    public void close() {
    }
}
