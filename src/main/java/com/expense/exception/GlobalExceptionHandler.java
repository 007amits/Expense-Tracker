package com.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<Map<String, String>> handleCategoryInUseException(CategoryInUseException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        
        // Check if this is a future date validation error
        if (ex.getMessage().contains("Future dates")) {
            response.put("error", "DATE_VALIDATION_ERROR");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
