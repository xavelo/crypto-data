package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.in.GetPricesUseCase;
import com.xavelo.crypto.application.port.out.GetPricesPort;
import com.xavelo.crypto.domain.model.Price;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetPricesService implements GetPricesUseCase {

    private static final Logger logger = LogManager.getLogger(GetPricesService.class);

    GetPricesPort getPricesPort;

    @Override
    public Price getLastPrice(String coin) {
        return getPricesPort.getLastPrice(coin);
    }

}