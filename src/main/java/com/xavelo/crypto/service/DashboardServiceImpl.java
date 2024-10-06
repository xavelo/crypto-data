package com.xavelo.crypto.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.xavelo.crypto.model.Price;
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
    
}