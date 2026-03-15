package com.ecom.order.exception;

public class CartEmptyException extends RuntimeException {

    public CartEmptyException(Long userId) {
        super("Cart is empty for userId=" + userId);
    }
}