package com.xavelo.crypto.domain.repository;

import com.xavelo.crypto.domain.model.CoinData;

public interface CoinDataRepository {

    void saveCoinData(CoinData coinData);

}
