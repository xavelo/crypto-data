package com.xavelo.crypto.data;

import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.adapter.mongo.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class DataServiceImpl implements DataService {

    @Autowired
    PriceRepository priceRepository;

    @Override
    public long getPricesCount() {
        return priceRepository.count();
    }

    @Override
    public long getPricesCount(String coin) {
        return priceRepository.countByCoin(coin);
    }

    @Override
    public List<PriceDocument> getPricesByCoinLastHours(String coin, int hours) {
        Instant hoursAgo = Instant.now().minus(hours, ChronoUnit.HOURS);
        return priceRepository.findPricesForCoinInLastHours(coin, hoursAgo);
    }

    @Override
    public BigDecimal getAveragePriceByCoinLastHours(String coin, int hours) {
        Date date = new Date(System.currentTimeMillis() - (long) hours * 60 * 60 * 1000);
        List<AveragePrice> result = priceRepository.findAveragePriceInLast24Hours(coin, date);
        return result.get(0).getValue();
    }

}