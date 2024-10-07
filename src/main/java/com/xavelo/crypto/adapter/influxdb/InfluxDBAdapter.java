package com.xavelo.crypto.adapter.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable; // Import for Flux queries

import io.micrometer.core.instrument.MeterRegistry;

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
        QueryApi queryApi = influxDBClient.getQueryApi();
    
        String query = "from(bucket: \"crypto\") "
                    + "|> range(start: -1h) "
                    + "|> filter(fn: (r) => r._measurement == \"crypto_price_updates\")"
                    + "|> filter(fn: (r) => r[\"coin\"] == \"ADA\")"
                    + "|> mean(column: \"_value\")";

        logger.info("influxdb query: {}", query);
    
        try {
            List<FluxTable> results = queryApi.query(query);
            if (results.isEmpty()) {
                logger.error("empty results");
                return 0.0;
            }
        
            FluxTable table = results.get(0);
            if (table.getRecords().isEmpty()) {
                logger.error("empty records");
                return 0.0;
            }
        
            FluxRecord record = table.getRecords().get(0);
            logger.info("record: {}", record.toString());
            Object o = record.getValueByIndex(0);
            logger.info("object {}", o);
            return (Double)record.getValueByKey("mean");
            
        } catch (com.influxdb.exceptions.BadRequestException e) {
            logger.error("Error executing query: {}", e.getMessage());
            return 0.0;
        }
    

    }

}
