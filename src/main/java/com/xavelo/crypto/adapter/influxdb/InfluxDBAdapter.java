package com.xavelo.crypto.adapter.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.xavelo.crypto.model.Price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;    
    
@Service
public class InfluxDBAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBAdapter.class);

    private final InfluxDBClient influxDBClient;

    public InfluxDBAdapter(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public void writePriceUpdate(Price price) {
        logger.info("writePriceUpdate");
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
            Point point = Point.measurement("crypto_price_updates")
                .addTag("coin", price.getCoin())
                .addTag("currency", price.getCurrency())
                .addField("price", price.getPrice().doubleValue())
                .time(price.getTimestamp(), WritePrecision.NS);
            
            writeApi.writePoint(point);
        }
    }

}
