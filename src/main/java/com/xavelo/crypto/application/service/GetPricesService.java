package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.GetPricesUseCase;
import com.xavelo.crypto.domain.model.AveragePrice;
import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.repository.PriceRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@AllArgsConstructor
public class GetPricesService implements GetPricesUseCase {

    private static final Logger logger = LogManager.getLogger(GetPricesService.class);

    PriceRepository priceRepository;

    @Override
    public Price getLatestPrice(String coin) {
        return priceRepository.getLatestPrice(coin);
    }

}