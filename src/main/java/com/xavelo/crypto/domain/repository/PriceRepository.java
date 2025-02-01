package com.xavelo.crypto.domain.repository;

import com.xavelo.crypto.domain.model.Price;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

public interface PriceRepository {

    @Service
    public interface PriceService {
        public void savePriceUpdate(Price price);
        public long getPriceUpdatesCount();
        public long getPriceUpdatesCountByCoin(String coin);
        public long getPriceUpdatesCountByCoinInRange(String coin, int range, String unit);
        public Price getLastPriceByCoin(String coin);
        public List<Price> getPriceUpdatesByCoin(String coin);
        public Price getHistoricalPriceByCoin(String coin, int range, String unit);
        public BigDecimal getAveragePriceByCoinInRange(String coin, int range, String unit);
    }

}
