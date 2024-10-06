package com.xavelo.crypto.api;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.xavelo.crypto.service.DashboardService;
import com.xavelo.crypto.model.Price; // Ensure this import is present

public class CryptoDashboardController {

    private static final Logger logger = LogManager.getLogger(CryptoDashboardController.class);

    private DashboardService dashboardService;

    public CryptoDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/prices")
    public ResponseEntity<List<Price>> listPrices() {
        return ResponseEntity.ok(dashboardService.getPrices());
    }

}
