package com.ecom.product.inventory.exception;

public class InvalidInventoryStateException extends RuntimeException {
    public InvalidInventoryStateException(String message) {
        super(message);
    }
}
