package com.xavelo.crypto.data;

import com.xavelo.crypto.adapter.mongo.PriceDocument;

import java.math.BigDecimal;
import java.util.List;

public interface DataService {

    long getPricesCount();

    long getPricesCount(String coin);

    List<PriceDocument> getPricesByCoinLastHours(String coin, int hours);

    BigDecimal getAveragePriceByCoinLastHours(String coin, int hours);

}
