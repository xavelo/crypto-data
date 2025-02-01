package com.xavelo.crypto.application.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.xavelo.crypto.domain.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.xavelo.crypto.domain.model.Price;


@Component
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private PriceRepository priceRepository;

    public DashboardServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }
    
    public List<Price> getPrices() {
        List<Price> prices = new ArrayList<>();
//        for (Coin coin : Coin.values()) {
//            prices.add(priceRepository.getLastPriceByCoin(coin.name()));
//        }
        return prices;
    }

    /*
    public Trend getTrend(String coin, int range, String unit) {
        logger.info(" ");
        logger.info("getTrend {} {}{}", coin, range, unit);
        Price currentPrice = priceService.getLastPriceByCoin(coin);
        Price historicalPrice = priceService.getHistoricalPriceByCoin(coin, range, unit);
        BigDecimal absoluteVariation = currentPrice.getPrice().subtract(historicalPrice.getPrice());
        double percentageVariation = absoluteVariation.divide(historicalPrice.getPrice(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
        logger.info("currentPrice {} - historicalPrice {} - percentage {}", currentPrice, historicalPrice, percentageVariation);
        return new Trend(coin, absoluteVariation.compareTo(BigDecimal.ZERO) >= 0, percentageVariation, absoluteVariation, currentPrice, historicalPrice);
    }
    */
    
}