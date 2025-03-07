package com.xavelo.crypto.domain.repository;

import com.xavelo.crypto.domain.model.Price;
import org.springframework.stereotype.Service;

@Service
public interface PriceRepository {

    public long countPriceUpdates(String coin);
    public Price getLatestPrice(String coin);

}
