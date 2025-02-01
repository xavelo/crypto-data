package com.xavelo.crypto.application.dashboard;

import org.springframework.stereotype.Service;

import com.xavelo.crypto.domain.model.Price;
import com.xavelo.crypto.domain.model.Trend; // Add this import statement

import java.util.List;

@Service
public interface DashboardService {
    
    public List<Price> getPrices();
    // public Trend getTrend(String coin, int range, String unit);

}
