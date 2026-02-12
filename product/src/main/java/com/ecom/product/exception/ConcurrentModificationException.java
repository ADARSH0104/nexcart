package com.ecom.product.exception;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException(String message) {
        super(message);
    }
}
