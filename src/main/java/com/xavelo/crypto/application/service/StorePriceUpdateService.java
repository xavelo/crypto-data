package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.in.ForStoringPriceUpdate;
import com.xavelo.crypto.application.port.out.StorePriceUpdatePort;
import com.xavelo.crypto.domain.model.Price;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StorePriceUpdateService implements ForStoringPriceUpdate {

    private static final Logger logger = LoggerFactory.getLogger(StorePriceUpdateService.class);

    private final StorePriceUpdatePort storePriceUpdatePort;

    @Override
    public void storePriceUpdate(Price price) {
        logger.info("StoringPrice Update - {}-{}: {}", price.getCoin(), price.getCurrency(), price.getPrice());
        storePriceUpdatePort.storePriceUpdate(price);
    }
}
