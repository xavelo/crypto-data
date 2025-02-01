package com.xavelo.crypto.domain.model;

public enum Coin {

    BTC("bitcoin"),
    ETH("ethereum"),    
    ADA("Cardano"),
    BNB("BNB"),
    DOT("Chainlink"),
    FET("Fetch AI"),
    HBAR("Hedera Hashgraph"),
    KAS("Kaspa"),
    LINK("Chainlink"),
    NEAR("NEAR Protocol"),
    RENDER("Render"),
    RUNE("Thorchain"),
    TRX("Tron"),
    XRP("Ripple");

    private final String fullName;

    Coin(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}

