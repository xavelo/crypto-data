package com.xavelo.crypto.domain.model;

import java.math.BigDecimal;

public class AveragePrice {
    private BigDecimal value;

    public AveragePrice(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
