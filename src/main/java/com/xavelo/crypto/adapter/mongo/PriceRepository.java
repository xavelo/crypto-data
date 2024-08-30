package com.xavelo.crypto.adapter.mongo;

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
    List<PriceDocument> findByTimestampAfter(Instant timestamp);

    @Query("{'id.coin': ?0, 'id.timestamp': {$gte: ?1}}")
    List<PriceDocument> findPricesForCoinInLastHours(String coin, Instant timestamp);

}
