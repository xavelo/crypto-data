package com.xavelo.crypto.data;

import com.xavelo.crypto.adapter.mongo.PriceDocument;

import java.util.List;

public interface DataService {

    long getPricesCount();

    long getPricesCount(String coin);

    List<PriceDocument> getPricesByCoinLastHours(String coin, int hours);

}
