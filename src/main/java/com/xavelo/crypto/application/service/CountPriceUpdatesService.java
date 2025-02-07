package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.CountPriceUpdatesUseCase;
import com.xavelo.crypto.domain.repository.PriceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CountPriceUpdatesService implements CountPriceUpdatesUseCase {

    private PriceRepository priceRepository;

    @Override
    public long countPriceUpdates(String coin) {
        return priceRepository.countPriceUpdates(coin);
    }

}
