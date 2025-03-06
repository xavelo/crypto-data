package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.out.StoreCoinDataUseCase;
import com.xavelo.crypto.domain.model.CoinData;
import com.xavelo.crypto.domain.repository.CoinDataRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StoreCoinDataService implements StoreCoinDataUseCase {

    private static final Logger logger = LoggerFactory.getLogger(StoreCoinDataService.class);

    private final CoinDataRepository coinDataRepository;

    @Override
    public void storeCoinData(CoinData coinData) {
        logger.info("Storing Coin Data - {} : {}", coinData.getSymbol(), coinData.getCurrentPrice());
        coinDataRepository.saveCoinData(coinData);
    }

}
