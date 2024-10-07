package com.xavelo.crypto.adapter.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit; 

import com.xavelo.crypto.model.Price; // Ensure the correct import for Price

@Component
public class InfluxDBAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBAdapter.class);

    private final InfluxDBClient influxDBClient;
    private final MeterRegistry meterRegistry;

    public InfluxDBAdapter(InfluxDBClient influxDBClient, MeterRegistry meterRegistry) {
        this.influxDBClient = influxDBClient;
        this.meterRegistry = meterRegistry;
        resetPriceData();
    }

    public void writePriceUpdate(Price price) {
        long startTime = System.nanoTime();
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("crypto_price_updates")
                .addTag("coin", price.getCoin())
                .addTag("currency", price.getCurrency())
                .addField("price", price.getPrice().toString())
                .time(price.getTimestamp(), WritePrecision.NS);
            
            writeApi.writePoint(point);
        }
        long endTime = System.nanoTime();
        long processingTime = (endTime - startTime) / 1_000_000;
        logger.info("crypto.price.save.influxdb.time: {}ms", processingTime);

        /*
        Timer timer = Timer.builder("crypto.price.save.influxdb.timee")
                .description("Time taken to save crypto price update to InfluxDB")
                .register(meterRegistry);
        timer.record(processingTime, TimeUnit.MILLISECONDS);
        */
    }

    private void resetPriceData() {
        influxDBClient.getQueryApi().query("DELETE FROM \"crypto_price_updates\"");
    }

}
