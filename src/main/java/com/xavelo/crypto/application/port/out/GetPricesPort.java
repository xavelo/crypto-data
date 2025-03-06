package com.xavelo.crypto.application.port.out;

import com.xavelo.crypto.domain.model.Price;

public interface GetPricesPort {

    Price getLastPrice(String coin);
    long countPriceUpdates();
    long countPriceUpdates(String coin);

}
