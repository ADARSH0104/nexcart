package com.ecom.notification.dto;

import java.math.BigDecimal;


public record OrderItemDetailsDTO(Long inventoryId,BigDecimal unitPrice, BigDecimal totalPrice, Long quantity) {
}
