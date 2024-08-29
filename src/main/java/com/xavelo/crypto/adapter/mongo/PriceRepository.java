package com.xavelo.crypto.adapter.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PriceRepository extends MongoRepository<PriceDocument, PriceDocument.PriceId> {

    @Query("{'_id.timestamp': {$gt: ?0}}")
    List<PriceDocument> findByTimestampAfter(Instant timestamp);

}
