package com.ecom.product.model;

public enum InventoryStatusEnum {

    INVENTORY_CREATED(100),

    STOCK_ADDED(200),

    PRICE_UPDATED(300),

    RESERVED(400),

    RELEASED(500),

    EXPIRED(600),

    CONFIRMED(700),

    RETURNED(800),

    ADJUSTED(900);

    private final int code;

    InventoryStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static InventoryStatusEnum fromCode(int code) {
        for (InventoryStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("Invalid InventoryStatus code: " + code);

    }
}
