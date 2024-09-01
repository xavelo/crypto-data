package com.xavelo.crypto.adapter.mongo;

import com.xavelo.crypto.data.AveragePrice;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface PriceRepository extends MongoRepository<PriceDocument, PriceDocument.PriceId> {

    long count();

    @Query("{ 'id.coin': ?0 }")
    List<PriceDocument>findByCoin(String coin);

    default long countByCoin(String coin) {
        return findByCoin(coin).size();
    }

    @Query("{'_id.timestamp': {$gt: ?0}}")
    List<PriceDocument> findByTimestampAfter(Instant timestamp);

    @Query("{'id.coin': ?0, 'id.timestamp': {$gte: ?1}}")
    List<PriceDocument> findPricesForCoinInLastHours(String coin, Instant timestamp);

    @Aggregation(pipeline = {
            "{ $match: { 'id.coin': ?0, timestamp: { $gte: ?1 } } }",
            "{ $group: { _id: null, avgPrice: { $avg: \"$price\" } } }"
    })
    List<AveragePrice> findAveragePriceInLast24Hours(String coin, Date date);

}
