package com.xavelo.crypto.application.port;

import com.xavelo.crypto.domain.model.Price;

public interface StorePriceUpdateUseCase {

    void storePriceUpdate(Price price);

}
