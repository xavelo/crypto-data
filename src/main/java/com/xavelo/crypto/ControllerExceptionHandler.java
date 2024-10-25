package com.xavelo.crypto;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.Data;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    private final Counter exceptionCounter;

    public ControllerExceptionHandler(MeterRegistry meterRegistry) {
        this.exceptionCounter = Counter.builder("crypto.price.processing.exceptions.total")
                .description("Total number of exceptions handled")
                .register(meterRegistry);
    }

    @Data
    public class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;

        public ErrorDetails(Date timestamp, String message, String details) {
            super();
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> badCredentialsHandler(Exception ex, WebRequest request) {
        logger.error("Exception {}", ex.getMessage(), ex);
        exceptionCounter.increment();
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
