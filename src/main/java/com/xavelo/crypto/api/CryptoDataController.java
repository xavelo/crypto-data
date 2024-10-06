package com.xavelo.crypto.api;

import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.data.DataService;
import com.xavelo.crypto.service.PriceService; // Moved to the correct position
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List; // Moved to the correct position

@RestController
public class CryptoDataController {

    private static final Logger logger = LogManager.getLogger(CryptoDataController.class);

    @Value("${HOSTNAME:unknown}")
    private String podName;

    private final DataService dataService;
    private final PriceService priceService;

    public CryptoDataController(DataService dataService, PriceService priceService) {
        this.dataService = dataService;
        this.priceService = priceService;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("hello from pod {}", podName);
        return ResponseEntity.ok("hello from pod " + podName);
    }

    @GetMapping("/prices/count")
    public ResponseEntity<Long> count() {
        long priceUpdatesCount = priceService.getPriceUpdatesCount();
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/prices/count/{coin}")
    public ResponseEntity<Long> countByCoin(@PathVariable String coin) {
        long priceUpdatesCount = priceService.getPriceUpdatesCountByCoin(coin);
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/prices/count/{coin}/{range}/{unit}")
    public ResponseEntity<Long> getPriceUpdatesCountByCoinInRange(@PathVariable String coin, @PathVariable int range, @PathVariable String unit) {
        long priceUpdatesCount = priceService.getPriceUpdatesCountByCoin(coin);
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/price/{coin}")
    public ResponseEntity<BigDecimal> getPriceByCoin(@PathVariable String coin) {
        BigDecimal price = priceService.getAveragePriceByCoin(coin);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/price/{coin}/average/{range}/{unit}")
    public ResponseEntity<BigDecimal> getAveragePriceByCoinInRange(@PathVariable String coin, @PathVariable int range, @PathVariable String unit) {
        BigDecimal average = priceService.getAveragePriceByCoinInRange(coin, range, unit);
        return ResponseEntity.ok(average);
    }

}

