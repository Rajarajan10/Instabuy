package com.ecommerce.order_service.exception;

public class UnauthorizedCartAccessException extends RuntimeException {
    public UnauthorizedCartAccessException(String message) {
        super(message);
    }
}
