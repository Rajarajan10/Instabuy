package com.ecommerce.order_service.exception;

public class StockUpdateException extends RuntimeException {
    public StockUpdateException(String message) {
        super(message);
    }
}
