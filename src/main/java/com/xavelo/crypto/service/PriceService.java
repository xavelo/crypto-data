package com.xavelo.crypto.service;

import com.xavelo.crypto.Price;
import java.math.BigDecimal; // Add this import statement

import org.springframework.stereotype.Service;

@Service
public interface PriceService {
    public void savePriceUpdate(Price price);
    public long getPriceUpdatesCount();
    public long getPriceUpdatesCountByCoin(String coin);
    public long getPriceUpdatesCountByCoinInRange(String coin, int range, String unit);
    public BigDecimal getLastPriceByCoin(String coin);
    public BigDecimal getAveragePriceByCoin(String coin);
    public BigDecimal getAveragePriceByCoinInRange(String coin, int range, String unit);
}
