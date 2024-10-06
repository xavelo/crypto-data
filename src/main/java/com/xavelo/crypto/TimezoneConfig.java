package com.xavelo.crypto;

import org.springframework.context.annotation.Configuration;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;

@Configuration
public class TimezoneConfig {
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    }
}