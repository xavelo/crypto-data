package com.xavelo.crypto.application.port;

import com.xavelo.crypto.domain.model.CoinData;

public interface GetCoinDataUseCase {

    CoinData getCoinData(String coin);

}
