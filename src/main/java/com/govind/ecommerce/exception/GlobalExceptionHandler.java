package com.govind.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "error", ex.getMessage()
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        String message = ex.getMessage() == null
                ? "Request failed"
                : ex.getMessage();

        if (message.toLowerCase().contains("insufficient stock")) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", message
                ));
    }
}