package com.ecom.order.model;

public enum OrderStatusEnum {
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_PENDING,
    PAID,
    COMPLETED,
    CANCELLED,
    EXPIRED
}