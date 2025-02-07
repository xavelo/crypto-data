package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.StorePriceUpdateUseCase;
import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.repository.PriceRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StorePriceUpdateService implements StorePriceUpdateUseCase {

    private static final Logger logger = LoggerFactory.getLogger(StorePriceUpdateService.class);

    private final PriceRepository priceRepository;

    @Override
    public void storePriceUpdate(Price price) {
        logger.info("StoringPrice Update - {} : {}", price.getCoin(), price.getPrice());
        priceRepository.savePriceUpdate(price);
    }
}
