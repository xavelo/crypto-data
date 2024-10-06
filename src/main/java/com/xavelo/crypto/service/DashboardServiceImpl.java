package com.xavelo.crypto.service;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.xavelo.crypto.model.Price;
import com.xavelo.crypto.model.Trend;
import com.xavelo.crypto.model.Coin;

@Component
public class DashboardServiceImpl implements DashboardService {

    private PriceService priceService;

    public DashboardServiceImpl(PriceService priceService) {
        this.priceService = priceService;
    }
    
    public List<Price> getPrices() {
        List<Price> prices = new ArrayList<>();
        for (Coin coin : Coin.values()) {            
            prices.add(priceService.getLastPriceByCoin(coin.name()));
        }
        return prices;
    }

    public Trend getTrend(String coin, int range, String unit) {
        // Get the current price
        Price currentPrice = priceService.getLastPriceByCoin(coin);
        
        // Get the historical price based on the range and unit
        Price historicalPrice = priceService.getHistoricalPriceByCoin(coin, range, unit);
        
        // Calculate absolute value and percentage variation using BigDecimal        
        BigDecimal absoluteVariation = currentPrice.getPrice().subtract(historicalPrice.getPrice());
        double percentageVariation = absoluteVariation.divide(historicalPrice.getPrice(), RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();

        // Create and return the Trend object
        return new Trend(coin, absoluteVariation.compareTo(BigDecimal.ZERO) >= 0, percentageVariation, absoluteVariation);
    }
    
}