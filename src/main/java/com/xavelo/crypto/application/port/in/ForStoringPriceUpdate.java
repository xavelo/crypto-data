package com.xavelo.crypto.application.port.in;

import com.xavelo.crypto.domain.model.Price;

public interface ForStoringPriceUpdate {
    void storePriceUpdate(Price price);
}