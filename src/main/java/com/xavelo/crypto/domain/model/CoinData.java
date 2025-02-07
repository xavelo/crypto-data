package com.xavelo.crypto.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.List;

@Getter
public class CoinData {
    @JsonProperty
    private String id;
    @JsonProperty
    private String symbol;
    @JsonProperty
    private String name;
    @JsonProperty
    private String image;
    @JsonProperty("current_price")
    private double currentPrice;
    @JsonProperty("market_cap")
    private long marketCap;
    @JsonProperty("market_cap_rank")
    private int marketCapRank;
    @JsonProperty("fully_diluted_valuation")
    private long fullyDilutedValuation;
    @JsonProperty("total_volume")
    private long totalVolume;
    @JsonProperty("high_24h")
    private double high24h;
    @JsonProperty("low_24h")
    private double low24h;
    @JsonProperty("price_change_24h")
    private double priceChange24h;
    @JsonProperty("price_change_percentage_24h")
    private double priceChangePercentage24h;
    @JsonProperty("market_cap_change_24h")
    private double marketCapChange24h;
    @JsonProperty("market_cap_change_percentage_24h")
    private double marketCapChangePercentage24h;
    @JsonProperty("circulating_supply")
    private double circulatingSupply;
    @JsonProperty("total_supply")
    private Double totalSupply;
    @JsonProperty("max_supply")
    private Double maxSupply;
    @JsonProperty("ath")
    private double ath;
    @JsonProperty("ath_change_percentage")
    private double athChangePercentage;
    @JsonProperty("ath_date")
    private String athDate;
    @JsonProperty
    private double atl;
    @JsonProperty("atl_change_percentage")
    private double atlChangePercentage;
    @JsonProperty("atl_date")
    private String atlDate;
    @JsonProperty
    private Roi roi;
    @JsonProperty("last_updated")
    private String lastUpdated;

    // Nested class for ROI
    public static class Roi {
        @JsonProperty
        private double times;
        @JsonProperty
        private String currency;
        @JsonProperty
        private double percentage;

        // Getters and setters
    }

    // Getters and setters for all fields (omitted for brevity)

    public static List<CoinData> parseJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, mapper.getTypeFactory()
                .constructCollectionType(List.class, CoinData.class));
    }

}
