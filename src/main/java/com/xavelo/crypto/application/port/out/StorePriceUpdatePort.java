package com.xavelo.crypto.application.port.out;

import com.xavelo.crypto.domain.model.Price;

public interface StorePriceUpdatePort {

    void storePriceUpdate(Price price);

}
