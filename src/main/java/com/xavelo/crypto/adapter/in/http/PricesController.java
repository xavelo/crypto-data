package com.xavelo.crypto.adapter.in.http;

import com.xavelo.crypto.application.port.in.CountPriceUpdatesUseCase;
import com.xavelo.crypto.application.port.in.GetPricesUseCase;
import com.xavelo.crypto.domain.model.Price;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PricesController {

    private static final Logger logger = LogManager.getLogger(PricesController.class);

    @Value("${HOSTNAME:unknown}")
    private String podName;

    @Autowired
    private final GetPricesUseCase getPricesUseCase;

    @Autowired
    private final CountPriceUpdatesUseCase countPriceUpdatesUseCase;

    public PricesController(GetPricesUseCase getPricesUseCase, CountPriceUpdatesUseCase countPriceUpdatesUseCase) {
        this.getPricesUseCase = getPricesUseCase;
        this.countPriceUpdatesUseCase = countPriceUpdatesUseCase;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("hello from pod {}", podName);
        return ResponseEntity.ok("hello from pod " + podName);
    }

    @GetMapping("/price/{coin}")
    public ResponseEntity<Price> getPriceByCoin(@PathVariable String coin) {
        Price price = getPricesUseCase.getLastPrice(coin);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/prices/count")
    public ResponseEntity<Long> count() {
        long priceUpdatesCount = countPriceUpdatesUseCase.countPriceUpdates();
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/prices/count/{coin}")
    public ResponseEntity<Long> countByCoin(@PathVariable String coin) {
        long priceUpdatesCount = countPriceUpdatesUseCase.countPriceUpdates(coin);
        return ResponseEntity.ok(priceUpdatesCount);
    }


}

