package com.xavelo.crypto;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import io.micrometer.core.instrument.Counter; // {{ edit_1 }} Import Micrometer Counter
import io.micrometer.core.instrument.MeterRegistry;

import lombok.Data;

@ControllerAdvice
public class ControllerExceptionHandler {

    private final Counter exceptionCounter;

    public ControllerExceptionHandler(MeterRegistry meterRegistry) {
        this.exceptionCounter = Counter.builder("exceptions.total") // {{ edit_6 }} Create Counter
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
        exceptionCounter.increment();
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
