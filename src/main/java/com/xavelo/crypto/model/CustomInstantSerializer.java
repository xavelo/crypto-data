package com.xavelo.crypto.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomInstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ZonedDateTime zdt = value.atZone(ZoneId.of("Europe/Madrid"));
        // Custom date format
        String formattedDate = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        gen.writeString(formattedDate);
    }
}
