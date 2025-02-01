package com.xavelo.crypto.domain.model.serdes;

import org.apache.kafka.common.serialization.Serdes;

import java.math.BigDecimal;

public class BigDecimalSerde extends Serdes.WrapperSerde<BigDecimal> {
    public BigDecimalSerde() {
        super(new BigDecimalSerializer(), new BigDecimalDeserializer());
    }
}
