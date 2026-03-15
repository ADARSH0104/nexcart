package com.ecom.notification.dto;

import java.math.BigDecimal;

public record OrderItemDetailsDTO(Long inventoryId, BigDecimal price, Long quantity) {
}
