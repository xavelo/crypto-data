package com.xavelo.crypto.application.data;

import com.xavelo.crypto.infrastructure.out.mongo.PriceDocument;
import com.xavelo.crypto.infrastructure.out.mongo.PriceRepository;
import com.xavelo.crypto.domain.model.AveragePrice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataServiceImpl implements DataService {

    private static final Logger logger = LogManager.getLogger(DataServiceImpl.class);

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
        Sort sort = Sort.by("_id.timestamp").descending();
        Instant hoursAgo = Instant.now().minus(hours, ChronoUnit.HOURS);
        return priceRepository.findPricesForCoinInLastHours(coin, hoursAgo, sort);
    }

    @Override
    public BigDecimal getAveragePriceByCoinLastHours(String coin, int hours) {
        Instant hoursAgo = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<AveragePrice> result = priceRepository.findAveragePriceInLast24Hours(coin, hoursAgo);
        logger.info("result {}", result);
        return (result != null && !result.isEmpty()) ? result.get(0).getValue() : null;
    }

}