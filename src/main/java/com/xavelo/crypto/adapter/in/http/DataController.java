package com.xavelo.crypto.adapter.in.http;

import com.xavelo.crypto.application.port.in.CountPriceUpdatesUseCase;
import com.xavelo.crypto.application.port.in.GetCoinDataUseCase;
import com.xavelo.crypto.application.port.in.GetPricesUseCase;
import com.xavelo.crypto.domain.model.CoinData;
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
public class DataController {

    private static final Logger logger = LogManager.getLogger(DataController.class);

    @Value("${HOSTNAME:unknown}")
    private String podName;

    @Autowired
    private final GetPricesUseCase getPricesUseCase;

    @Autowired
    private final CountPriceUpdatesUseCase countPriceUpdatesUseCase;

    @Autowired
    private final GetCoinDataUseCase getCoinDataUseCase;

    public DataController(GetPricesUseCase getPricesUseCase, GetCoinDataUseCase getCoinDataUseCase, CountPriceUpdatesUseCase countPriceUpdatesUseCase) {
        this.getPricesUseCase = getPricesUseCase;
        this.getCoinDataUseCase = getCoinDataUseCase;
        this.countPriceUpdatesUseCase = countPriceUpdatesUseCase;
    }

    /*

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        logger.info("hello from pod {}", podName);
        return ResponseEntity.ok("hello from pod " + podName);
    }

    @GetMapping("/data/{coin}")
    public ResponseEntity<CoinData> getDataByCoin(@PathVariable String coin) {
        CoinData coinData = getCoinDataUseCase.getCoinData(coin);
        return ResponseEntity.ok(coinData);
    }

    @GetMapping("/prices/count/{coin}/{range}/{unit}")
    public ResponseEntity<Long> getPriceUpdatesCountByCoinInRange(@PathVariable String coin, @PathVariable int range, @PathVariable String unit) {
        long priceUpdatesCount = priceRepository.getPriceUpdatesCountByCoin(coin);
        return ResponseEntity.ok(priceUpdatesCount);
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
    */


}

