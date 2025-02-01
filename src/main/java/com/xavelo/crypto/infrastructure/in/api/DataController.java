package com.xavelo.crypto.infrastructure.in.api;

import com.xavelo.crypto.domain.repository.PriceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import com.xavelo.crypto.domain.model.Price;
import java.util.List;

@RestController
public class DataController {

    private static final Logger logger = LogManager.getLogger(DataController.class);

    @Value("${HOSTNAME:unknown}")
    private String podName;

    private final PriceRepository priceRepository;

    public DataController(PriceRepository priceRepositorye) {
        this.priceRepository = priceRepositorye;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("hello from pod {}", podName);
        return ResponseEntity.ok("hello from pod " + podName);
    }

    @GetMapping("/test-error")
    public ResponseEntity<String> testError() throws Exception {
        throw new Exception("testing exceptino handler");
    }

    @GetMapping("/prices/count")
    public ResponseEntity<Long> count() {
        long priceUpdatesCount = priceRepository.getPriceUpdatesCount();
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/prices/count/{coin}")
    public ResponseEntity<Long> countByCoin(@PathVariable String coin) {
        long priceUpdatesCount = priceRepository.getPriceUpdatesCountByCoin(coin);
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/prices/count/{coin}/{range}/{unit}")
    public ResponseEntity<Long> getPriceUpdatesCountByCoinInRange(@PathVariable String coin, @PathVariable int range, @PathVariable String unit) {
        long priceUpdatesCount = priceRepository.getPriceUpdatesCountByCoin(coin);
        return ResponseEntity.ok(priceUpdatesCount);
    }

    @GetMapping("/price/{coin}")
    public ResponseEntity<Price> getPriceByCoin(@PathVariable String coin) {
        Price price = priceRepository.getLastPriceByCoin(coin);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/prices/{coin}")
    public ResponseEntity<List<Price>> getPricesByCoin(@PathVariable String coin) {
        List<Price> prices = priceRepository.getPriceUpdatesByCoin(coin);
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/price/{coin}/average/{range}/{unit}")
    public ResponseEntity<BigDecimal> getAveragePriceByCoinInRange(@PathVariable String coin, @PathVariable int range, @PathVariable String unit) {
        BigDecimal average = priceRepository.getAveragePriceByCoinInRange(coin, range, unit);
        return ResponseEntity.ok(average);
    }

}

