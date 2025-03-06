package com.xavelo.crypto.application.port.in;

public interface CountPriceUpdatesUseCase {

    long countPriceUpdates();
    long countPriceUpdates(String coin);

}
