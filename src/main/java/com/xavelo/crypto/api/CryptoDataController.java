package com.xavelo.crypto.api;

import com.xavelo.crypto.Price;
import com.xavelo.crypto.adapter.mongo.PriceDocument;
import com.xavelo.crypto.data.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CryptoDataController {

    private static final Logger logger = LogManager.getLogger(CryptoDataController.class);

    @Value("${HOSTNAME:unknown}")
    private String podName;

    @Autowired
    DataService dataService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("hello from pod {}", podName);
        return ResponseEntity.ok("hello from pod " + podName);
    }

    @GetMapping("/prices/count")
    public ResponseEntity<Long> count() {
        long count = dataService.getPricesCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/prices/count/{coin}")
    public ResponseEntity<Long> countByCoin(@PathVariable String coin) {
        long count = dataService.getPricesCount(coin);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/prices/{coin}/last/{hours}/h")
    public ResponseEntity<List<PriceDocument>> lastPricesByCoin(@PathVariable String coin, @PathVariable int hours) {
        List<PriceDocument> prices = dataService.getPricesByCoinLastHours(coin, hours);
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/price/{coin}/average/{hours}/h")
    public ResponseEntity<BigDecimal> averagePricesByCoin(@PathVariable String coin, @PathVariable int hours) {
        BigDecimal averagePrice = dataService.getAveragePriceByCoinLastHours(coin, hours);
        return ResponseEntity.ok(averagePrice);
    }

}

