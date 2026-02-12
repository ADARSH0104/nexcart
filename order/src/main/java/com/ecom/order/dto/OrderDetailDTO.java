package com.ecom.order.dto;

import java.math.BigDecimal;

public record OrderDetailDTO(Long inventoryId, Long quantity, BigDecimal totalPrice,BigDecimal unitPrice,Boolean isOutOfStock,Boolean isQuantityChanged,Boolean isPriceChanged) {
}
