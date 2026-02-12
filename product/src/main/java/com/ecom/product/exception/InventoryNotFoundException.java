package com.ecom.product.exception;

public class InventoryNotFoundException extends RuntimeException  {

    public InventoryNotFoundException(String message){
        super(message);
    }
}
