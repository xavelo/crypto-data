package com.xavelo.crypto.application.port.in;

import com.xavelo.crypto.domain.model.Price;

public interface GetPricesUseCase {

    Price getLastPrice(String coin);

    /*

    List<PriceDocument> getPricesByCoinLastHours(String coin, int hours);

    BigDecimal getAveragePriceByCoinLastHours(String coin, int hours);
    */
}
