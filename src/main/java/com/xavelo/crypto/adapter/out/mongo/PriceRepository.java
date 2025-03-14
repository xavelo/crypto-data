package com.xavelo.crypto.adapter.out.mongo;

import com.xavelo.crypto.domain.model.AveragePrice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PriceRepository extends MongoRepository<PriceDocument, PriceDocument.PriceId> {

    long count();

    @Query("{ 'id.coin': ?0 }")
    List<PriceDocument>findByCoin(String coin);

    default long countByCoin(String coin) {
        return findByCoin(coin).size();
    }

    @Query("{'_id.timestamp': {$gt: ?0}}")
    List<PriceDocument> findByTimestampAfter(Instant timestamp, Sort sort);

    @Query("{'id.coin': ?0, 'id.timestamp': {$gte: ?1}}")
    List<PriceDocument> findPricesForCoinInLastHours(String coin, Instant timestamp, Sort sort);

    @Aggregation(pipeline = {
            //"{ $match: { 'id.coin': ?0, timestamp: { $gte: ?1 } } }",
            "{ $match: { 'id.coin': ?0 } }",
            "{ $group: { _id: null, avgPrice: { $avg: \"$price\" } } }"
    })
    List<AveragePrice> findAveragePriceInLast24Hours(String coin, Instant timestamp);

}
