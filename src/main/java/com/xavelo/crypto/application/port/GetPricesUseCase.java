package com.xavelo.crypto.application.port;

import com.xavelo.crypto.adapter.out.mongo.PriceDocument;

import java.math.BigDecimal;
import java.util.List;

public interface GetPricesUseCase {

    long getPricesCount();

    long getPricesCount(String coin);

    List<PriceDocument> getPricesByCoinLastHours(String coin, int hours);

    BigDecimal getAveragePriceByCoinLastHours(String coin, int hours);

}
