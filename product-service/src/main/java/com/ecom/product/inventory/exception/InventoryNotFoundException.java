package com.ecom.product.inventory.exception;

public class InventoryNotFoundException extends RuntimeException  {

    public InventoryNotFoundException(String message){
        super(message);
    }
}
