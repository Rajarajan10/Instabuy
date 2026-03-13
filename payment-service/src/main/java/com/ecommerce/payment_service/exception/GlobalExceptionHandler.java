package com.ecommerce.payment_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Payment not found
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> handlePaymentNotFound(PaymentNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Duplicate payment for same order
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDuplicatePayment(DataIntegrityViolationException ex) {
        return new ResponseEntity<>("Payment already exists for this order.", HttpStatus.BAD_REQUEST);
    }

    // Invalid enum values (payment method, status)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidEnum(IllegalArgumentException ex) {
        return new ResponseEntity<>("Invalid payment method or status provided.", HttpStatus.BAD_REQUEST);
    }

    // Invalid JSON request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>("Malformed JSON request body.", HttpStatus.BAD_REQUEST);
    }

    // Wrong type in request parameter
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>("Invalid parameter type in request.", HttpStatus.BAD_REQUEST);
    }

    // Catch-all unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("Unexpected error occurred in Payment Service.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}