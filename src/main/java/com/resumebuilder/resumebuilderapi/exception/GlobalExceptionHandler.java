package com.resumebuilder.resumebuilderapi.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.info("Inside GlobalExceptionHandler - handleValidationException()");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError)error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("message", "validatation failed");
        response.put("errors", errors);

        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler (ResourceExistsException.class)
    public ResponseEntity<Map<String, Object>> handleResourceExistsException(ResourceExistsException ex) {
        log.info("Inside GlobalExceptionHandler - handleResouceExistsException()");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "resource exists");
//        response.put("status", HttpStatus.CONFLICT);
        response.put("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException (Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "internal server error, contact admin");
        response.put("errors", ex.getMessage());

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}