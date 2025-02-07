package com.xavelo.crypto.application.port;

import com.xavelo.crypto.domain.model.Price;

public interface GetPricesUseCase {

    Price getLatestPrice(String coin);

    /*
    long getPricesCount();

    long getPricesCount(String coin);

    List<PriceDocument> getPricesByCoinLastHours(String coin, int hours);

    BigDecimal getAveragePriceByCoinLastHours(String coin, int hours);
    */
}
