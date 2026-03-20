package com.ecommerce.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // NOT FOUND
    @ExceptionHandler({
            OrderNotFoundException.class,
            CartNotFoundException.class,
            CartItemNotFoundException.class,
            ProductNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // BAD REQUEST
    @ExceptionHandler({
            InvalidOrderStateException.class,
            InvalidQuantityException.class,
            EmptyCartException.class,
            InsufficientStockException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // SERVICE / PROCESSING ERRORS
    @ExceptionHandler({
            InventoryServiceException.class,
            PaymentFailedException.class,
            StockUpdateException.class,
            OrderCreationException.class
    })
    public ResponseEntity<Map<String, String>> handleServiceErrors(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }

    // UNAUTHORIZED
    @ExceptionHandler(UnauthorizedCartAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    // FALLBACK
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }
}