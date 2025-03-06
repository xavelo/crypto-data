package com.xavelo.crypto.application.service;

import com.xavelo.crypto.application.port.in.GetCoinDataUseCase;
import com.xavelo.crypto.domain.model.CoinData;
import com.xavelo.crypto.domain.repository.CoinDataRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetCoinDataService implements GetCoinDataUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetCoinDataService.class);

    private final CoinDataRepository coinDataRepository;

    @Override
    public CoinData getCoinData(String coin) {
        logger.info("Getting Coin Data - {}", coin);
        return coinDataRepository.getCoinData(coin);
    }

}
