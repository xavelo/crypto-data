package com.xavelo.crypto.application.port;

import com.xavelo.crypto.domain.model.CoinData;

public interface StoreCoinDataUseCase {

    void storeCoinData(CoinData coinData);

}
