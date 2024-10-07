package com.xavelo.crypto.adapter.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable; // Import for Flux queries

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit; 
import java.util.List;

import com.xavelo.crypto.model.Price; // Ensure the correct import for Price

@Component
public class InfluxDBAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBAdapter.class);

    private final InfluxDBClient influxDBClient;
    private final MeterRegistry meterRegistry;

    public InfluxDBAdapter(InfluxDBClient influxDBClient, MeterRegistry meterRegistry) {
        this.influxDBClient = influxDBClient;
        this.meterRegistry = meterRegistry;        
    }

    public void writePriceUpdate(Price price) {        
        long startTime = System.nanoTime();
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("crypto_price_updates")
                .addTag("coin", price.getCoin())
                .addTag("currency", price.getCurrency())
                .addField("price", price.getPrice().doubleValue())
                .time(price.getTimestamp(), WritePrecision.NS);
            
            writeApi.writePoint(point);
        }
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        logger.info("crypto.price.save.influxdb.time: {}ms", processingTime);

        Double averageLast1h = getAveragePrice(price.getCoin(), 1, "h");
        logger.info("1h average {} price: {}", price.getCoin(), averageLast1h);

        /*
        Timer timer = Timer.builder("crypto.price.save.influxdb.timee")
                .description("Time taken to save crypto price update to InfluxDB")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        */
    }

    public Double getAveragePrice(String coin, int range, String unit) {
        // Convert range and unit to a time filter for the query
        String timeFilter = String.format("now() - %d%s", range, unit);
        
        // Query the database for the average value of the specified coin
        String query = String.format("SELECT MEAN(_value) FROM %s WHERE time > %s", coin, timeFilter);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query);
        // Assuming the first table contains the result
        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            return (Double) tables.get(0).getRecords().get(0).getValueByKey("_value"); // Cast to Double
        }
        return null; // or throw an exception if no value is found
    }    

}
