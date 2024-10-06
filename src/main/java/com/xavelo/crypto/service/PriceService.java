package com.xavelo.crypto.service;

import java.math.BigDecimal; // Add this import statement

import org.springframework.stereotype.Service;

import com.xavelo.crypto.model.Price;

@Service
public interface PriceService {
    public void savePriceUpdate(Price price);
    public long getPriceUpdatesCount();
    public long getPriceUpdatesCountByCoin(String coin);
    public long getPriceUpdatesCountByCoinInRange(String coin, int range, String unit);
    public Price getLastPriceByCoin(String coin);
    public Price getHistoricalPriceByCoin(String coin, int range, String unit);
    public BigDecimal getAveragePriceByCoin(String coin);
    public BigDecimal getAveragePriceByCoinInRange(String coin, int range, String unit);
}
