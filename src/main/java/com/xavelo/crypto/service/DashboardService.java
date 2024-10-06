package com.xavelo.crypto.service;

import org.springframework.stereotype.Service;

import com.xavelo.crypto.model.Price;

import java.util.List;

@Service
public interface DashboardService {
    
    public List<Price> getPrices();

}
