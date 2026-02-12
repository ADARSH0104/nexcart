package com.ecom.product.exception;

public class InvalidInventoryStateException extends RuntimeException {
    public InvalidInventoryStateException(String message) {
        super(message);
    }
}
