package com.xavelo.crypto.api;

import com.xavelo.crypto.data.DataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/count")
    public ResponseEntity<String> consume(@RequestParam String topic) {
        long count = dataService.getPricesCount();
        return ResponseEntity.ok(topic);
    }

}

