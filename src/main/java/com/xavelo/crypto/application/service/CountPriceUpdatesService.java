package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.in.CountPriceUpdatesUseCase;
import com.xavelo.crypto.application.port.out.GetPricesPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CountPriceUpdatesService implements CountPriceUpdatesUseCase {

    private GetPricesPort getPricesPort;

    @Override
    public long countPriceUpdates() {
        return getPricesPort.countPriceUpdates();
    }

    @Override
    public long countPriceUpdates(String coin) {
        return getPricesPort.countPriceUpdates(coin);
    }

}
