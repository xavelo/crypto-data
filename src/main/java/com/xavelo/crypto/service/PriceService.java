package com.xavelo.crypto.service;

import com.xavelo.crypto.Price;
import java.math.BigDecimal; // Add this import statement

import org.springframework.stereotype.Service;

@Service
public interface PriceService {
    public void savePriceUpdate(Price price);
    public BigDecimal getLastPriceByCoin(String coin);
    public BigDecimal getAveragePriceByCoin(String coin);
}
