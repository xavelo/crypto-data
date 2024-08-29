package com.xavelo.crypto.adapter.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceRepository extends MongoRepository<PriceDocument, PriceDocument.CryptoPriceId> { }
