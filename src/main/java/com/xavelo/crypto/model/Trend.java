package com.xavelo.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal; // Add this import statement

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trend {
    
    private String coin;
    private boolean positive;
    private double percentage;
    private BigDecimal value;
    private Price currenPrice;
    private Price historialPrice;

}
